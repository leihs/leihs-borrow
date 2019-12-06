require 'spec_helper'
require_relative '../graphql/graphql_helper'

describe 'feature' do
  example 'search and order' do
    # prepare data for user:
    my_order_purpose = 'Filmdreh'
    my_start_date = (DateTime.now.to_date + 2.days).iso8601
    my_end_date = (DateTime.now.to_date + 3.days).iso8601

    # prepare data in DB:
    user = FactoryBot.create(:user)
    pool_1 = FactoryBot.create(:inventory_pool,
                               id: '8e484119-76a4-4251-b37b-64847df99e9b',
                               name: 'Pool A')
    pool_2 = FactoryBot.create(:inventory_pool,
                               id: 'a7d2e049-56ac-481a-937e-ee3f613f3cc7',
                               name: 'Pool B')
    FactoryBot.create(:access_right, role: :customer, user: user, inventory_pool: pool_1)
    FactoryBot.create(:access_right, role: :customer, user: user, inventory_pool: pool_2)
    categories = { film: FactoryBot.create(:category, name: 'Film') }
    model_names = [['Kamera', 'f616b467-80f5-45d7-b708-08c00d506a92'],
                   ['Stativ', 'e167e9c9-a298-46da-acc1-c1324b12f43a'],
                   ['Mikrofon', '0a34281f-0373-46f5-bbe7-f1d868f3d7a4']]
    models =
      model_names.map do |name, uuid|
        m = FactoryBot.create(:leihs_model, id: uuid, product: name, categories: [categories[:film]])
        2.times do
          FactoryBot.create(:item, leihs_model: m, owner: pool_1, responsible: pool_1)
        end
        2.times do
          FactoryBot.create(:item, leihs_model: m, owner: pool_2, responsible: pool_2)
        end
        [name.downcase, m]
      end.to_h
        .deep_symbolize_keys

    # STEP 0: fetch initial data to do a search
    get_search_filters_query = <<-GRAPHQL
      query getSearchFilters {
        mainCategories: categories(rootOnly: true) {
          id
          label: name
        }
        pools: inventoryPools(orderBy: [{attribute: ID, direction: ASC}]) {
          id
          label: name
        }
      }
    GRAPHQL

    expect_graphql_result(
      query(get_search_filters_query, user.id),
      {
        mainCategories: [{ id: categories[:film].id, label: categories[:film].name }],
        pools: [
          { id: pool_1.id, label: pool_1.name },
          { id: pool_2.id, label: pool_2.name },
        ]
      }
    )

    # STEP 1: search for a Kamera
    operation = File.read('src/all/leihs/borrow/client/queries/searchModels.gql')
    variables = { searchTerm: 'Kamera', startDate: my_start_date, endDate: my_end_date }

    search_result_1 = query(operation, user.id, variables)
    expect_graphql_result(
      search_result_1,
      {
        models: {
          edges: [
            { node: {
              description: nil,
              id: models[:kamera].id,
              images: [],
              manufacturer: nil,
              name: 'Kamera',
              availableQuantityInDateRange: 4
            }}
          ]
        }
      }
    )

    # STEP 1B: search for Stativ
    # STEP 1C: search for Mikrofon

    # STEP 2: add item to cart/order by creating a reservation
    operation = <<-GRAPHQL
      mutation addModelToOrder(
        $modelId: UUID!
        $quantity: Int!
        $startDate: Date!
        $endDate: Date!
      ) {
        reservations: createReservation(
          modelId: $modelId
          startDate: $startDate
          endDate: $endDate
          quantity: $quantity
        ) {
          id
        }
      }
    GRAPHQL

    variables = {
      endDate: my_end_date,
      startDate: my_start_date,
      modelId: models[:kamera].id,
      quantity: 3
    }

    reservation_result_1 = query(operation, user.id, variables)
    # look for this reservation in the DB, so we know the ID:
    expect(
      reservation_result_1.dig(:data, :reservations).map { |r| r[:id] }.to_set
    ).to eq Reservation.select(:id).all.map(&:id).to_set

    # STEP 2B: increase quantity of a reservation for a specific pool
    
    operation = <<-GRAPHQL
      mutation increaseQuantity(
        $modelId: UUID!
        $quantity: Int!
        $startDate: Date!
        $endDate: Date!
        $inventoryPoolIds: [UUID!]
      ) {
        reservations: createReservation(
          modelId: $modelId
          startDate: $startDate
          endDate: $endDate
          quantity: $quantity
          inventoryPoolIds: $inventoryPoolIds
        ) {
          id
        }
      }
    GRAPHQL
    
    variables = {
      endDate: my_end_date,
      startDate: my_start_date,
      modelId: models[:kamera].id,
      quantity: 1,
      inventoryPoolIds: [pool_2.id]
    }

    reservation_result_2 = query(operation, user.id, variables)
    expect(
      reservation_result_2.dig(:data, :reservations).map { |r| r[:id] }.to_set
    ).to eq \
      Reservation
      .select(:id)
      .order(Sequel.desc(:created_at))
      .limit(1)
      .map(&:id)
      .to_set

    # STEP 2C: decrease quantity of a reservation for a specific pool

    operation = <<-GRAPHQL
      mutation($ids: [UUID!]) {
        rIds: deleteReservations(
          ids: $ids
        )
      }
    GRAPHQL

    r_ids = Reservation.where(inventory_pool_id: pool_1.id).limit(1).all.map(&:id)

    variables = {
      ids: r_ids
    }

    reservation_result_2 = query(operation, user.id, variables)
    expect(reservation_result_2.dig(:data, :rIds)).to eq r_ids

    # STEP 2D: add a reservation for another model
    
    # STEP 2E: delete a reservation (= decrease quantity to 0)
    
    # TODO: STEP 2F: change date range of a reservation

    # STEP 3: submit the current order
    operation = <<-GRAPHQL
      mutation submitOrder($purpose: String!) {
        order: submitOrder(purpose: $purpose) {
          id
          purpose
          state
        }
      }
    GRAPHQL

    variables = { purpose: my_order_purpose }

    submit_order_result_1 = query(operation, user.id, variables)
    the_order_1 = Order.last
    the_pool_order_1, the_pool_order_2 = PoolOrder.order(:inventory_pool_id).all
    expect_graphql_result(
      submit_order_result_1,
      { order: { id: the_order_1.id, purpose: my_order_purpose, state: %w[SUBMITTED] } }
    )

    # STEP 4: see the order with status in the list of orders
    all_my_orders_operation_1 = <<-GRAPHQL
      query allMyOrders {
        orders {
          edges {
            node {
              id
              purpose
              state
              subOrdersByPool(orderBy: [{attribute: INVENTORY_POOL_ID, direction: ASC}]) {
                id
                state
                rejectedReason
                inventoryPool {
                  id
                  name
                }
                reservations {
                  startDate
                  endDate
                  model { id }
                  status
                }
              }
            }
          }
        }
      }
    GRAPHQL

    variables = { purpose: my_order_purpose }

    all_my_orders_result_1 = query(all_my_orders_operation_1, user.id, variables)
    expect_graphql_result(
      all_my_orders_result_1,
      {
        orders: {
          edges: [
            { node: {
              id: the_order_1.id,
              purpose: my_order_purpose,
              state: %w[SUBMITTED],
              subOrdersByPool: [
                {
                  id: the_pool_order_1.id,
                  inventoryPool: {
                    id: pool_1.id,
                    name: 'Pool A'
                  },
                  rejectedReason: nil,
                  reservations: [
                    {
                      model: { id: models[:kamera].id },
                      endDate: my_end_date,
                      startDate: my_start_date,
                      status: 'SUBMITTED'
                    }
                  ],
                  state: 'SUBMITTED'
                },
                {
                  id: the_pool_order_2.id,
                  inventoryPool: {
                    id: pool_2.id,
                    name: 'Pool B'
                  },
                  rejectedReason: nil,
                  reservations: [
                    {
                      model: { id: models[:kamera].id },
                      endDate: my_end_date,
                      startDate: my_start_date,
                      status: 'SUBMITTED'
                    },
                    {
                      model: { id: models[:kamera].id },
                      endDate: my_end_date,
                      startDate: my_start_date,
                      status: 'SUBMITTED'
                    }
                  ],
                  state: 'SUBMITTED'
                }
              ]
            } }
          ]
        }
      }
    )

    # STEP 4: backoffice approves the order
    fake_approve_order(the_pool_order_1)

    # STEP 5: see the new order status
    all_my_orders_operation_2 = <<-GRAPHQL
      query allMyOrders {
        orders {
          edges {
            node {
              state
              subOrdersByPool(orderBy: [{attribute: INVENTORY_POOL_ID, direction: ASC}]) {
                id
                state
                inventoryPool {
                  id
                }
              }
            }
          }
        }
      }
    GRAPHQL

    all_my_orders_result_2 = query(all_my_orders_operation_2, user.id, variables)
    expect_graphql_result(
      all_my_orders_result_2,
      { orders:
        { edges:
          [ { node:
              { state: %w[APPROVED SUBMITTED],
                subOrdersByPool: [
                  { id: the_pool_order_1.id,
                    state: 'APPROVED',
                    inventoryPool: {
                      id: '8e484119-76a4-4251-b37b-64847df99e9b'
                    }
                  },
                  { id: the_pool_order_2.id,
                    state: 'SUBMITTED',
                    inventoryPool: {
                      id: 'a7d2e049-56ac-481a-937e-ee3f613f3cc7' 
                    }
                  }
                ]
              }
            }
          ]
        }
      }
    )
  end
end

def fake_approve_order(order)
  database.transaction do
    order.update(state: 'approved')
    order.reservations.each { |item_line| item_line.update(status: 'approved') }
  end
end
