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
    pool = FactoryBot.create(:inventory_pool, id: '8e484119-76a4-4251-b37b-64847df99e9b')
    FactoryBot.create(:access_right, role: :customer, user: user, inventory_pool: pool)
    categories = { film: FactoryBot.create(:category, name: 'Film') }
    model_names = [['Kamera', 'f616b467-80f5-45d7-b708-08c00d506a92'],
                   ['Stativ', 'e167e9c9-a298-46da-acc1-c1324b12f43a'],
                   ['Mikrofon', '0a34281f-0373-46f5-bbe7-f1d868f3d7a4']]
    models =
      model_names.map do |name, uuid|
        m = FactoryBot.create(:leihs_model, id: uuid, product: name, categories: [categories[:film]])
        FactoryBot.create(:item, leihs_model: m, owner: pool, responsible: pool)
        [name.downcase, m]
      end.to_h
        .deep_symbolize_keys

    # STEP 0: fetch initial data to do a search
    op_get_search_filters = File.read('src/all/leihs/borrow/client/queries/getSearchFilters.gql')
    expect_graphql_result(
      query(op_get_search_filters, user.id),
      {
        mainCategories: [{ id: categories[:film].id, label: categories[:film].name }],
        pools: [{ id: pool.id, label: pool.name }]
      }
    )

    # STEP 1: search for a Kamera
    operation = File.read('src/all/leihs/borrow/client/queries/searchModels.gql')
    variables = { searchTerm: 'Kamera', startDate: my_start_date, endDate: my_end_date, pools: [pool.id] }

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
              availableQuantityInDateRange: 1
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
        $startDate: String!
        $endDate: String!
      ) {
        reservation: createReservation(
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
      quantity: search_result_1.dig(:data, :models, :edges).first.dig(:node, :availableQuantityInDateRange)
    }

    reservation_result_1 = query(operation, user.id, variables)
    # look for this reservation in the DB, so we know the ID:
    the_reservation_1 = Reservation.last
    expect_graphql_result(reservation_result_1, { reservation: [{ id: the_reservation_1.id }] })

    # binding.pry

    # STEP 2B: add more reservations
    # STEP 2C: delete a reservation

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

    reservation_result_1 = query(operation, user.id, variables)
    the_order_1 = Order.last
    the_pool_order_1 = PoolOrder.last
    expect_graphql_result(
      reservation_result_1,
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
              subOrdersByPool {
                id
                state
                rejectedReason
                inventoryPool { id }
                reservations {
                  id
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
                  inventoryPool: { id: pool.id },
                  rejectedReason: nil,
                  reservations: [
                    {
                      id: the_reservation_1.id,
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
              subOrdersByPool {
                state
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
              { state: %w[APPROVED], subOrdersByPool: [{ state: 'APPROVED' }] }
          }]
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
