require 'spec_helper'
require_relative 'graphql_helper'

describe 'orders' do
  before :example do
    Settings.first.update(deliver_received_order_notifications: true)
    SystemAndSecuritySettings.first.update(external_base_url: LEIHS_BORROW_HTTP_BASE_URL)
  end

  let(:user) do
    FactoryBot.create(
      :user,
      id: '8c360361-f70c-4b31-a271-b4050d4b9d26'
    )
  end

  let(:inventory_pool_1) do
    FactoryBot.create(
      :inventory_pool,
      id: '8633ce17-37da-4802-a377-66ca78291d0a'
    )
  end

  let(:inventory_pool_2) do
    FactoryBot.create(
      :inventory_pool,
      id: '4e2f1362-0891-4df7-b760-16a2a8d3373f',
      is_active: false
    )
  end

  let(:model_1) do
    FactoryBot.create(:leihs_model,
                      id: '98d398e7-08b3-49d4-807c-42a3eac07de9')
  end

  let(:model_2) do
    FactoryBot.create(:leihs_model,
                      id: '34d2f947-1ec1-474c-9afd-4b71b6c1d966')
  end

  let(:model_3) do
    FactoryBot.create(:leihs_model,
                      id: 'fd3cef3d-578c-4409-b814-eb20a46da21d')
  end

  before(:example) do
    FactoryBot.create(:direct_access_right,
                      inventory_pool: inventory_pool_1,
                      user: user)

    FactoryBot.create(:direct_access_right,
                      inventory_pool: inventory_pool_2,
                      user: user)
  end

  context 'create' do
    it 'succeeds' do
      model_1.add_item(
        FactoryBot.create(:item,
                          is_borrowable: true,
                          responsible: inventory_pool_1)
      )

      model_1.add_item(
        FactoryBot.create(:item,
                          is_borrowable: true,
                          responsible: inventory_pool_2)
      )

      model_2.add_item(
        FactoryBot.create(:item,
                          is_borrowable: true,
                          responsible: inventory_pool_2)
      )

      start_date = Date.tomorrow
      end_date = start_date + 1.day

      FactoryBot.create(:reservation,
                        id: '100ffcc9-5401-415b-9185-5fffa8e5c526',
                        leihs_model: model_1,
                        inventory_pool: inventory_pool_1,
                        start_date: start_date,
                        end_date: end_date,
                        user: user)
      FactoryBot.create(:reservation,
                        id: '20fbda2e-9265-4728-8e70-418c2b348d8a',
                        leihs_model: model_1,
                        inventory_pool: inventory_pool_2,
                        start_date: start_date,
                        end_date: end_date,
                        user: user)
      FactoryBot.create(:reservation,
                        id: 'bf7080fb-2118-472e-8cff-50a51d648389',
                        leihs_model: model_2,
                        inventory_pool: inventory_pool_2,
                        start_date: start_date,
                        end_date: end_date,
                        user: user)

      q = <<-GRAPHQL
        mutation(
          $purpose: NonEmptyText!
          $title: NonEmptyText!
        ) {
          submitOrder(
            purpose: $purpose
            title: $purpose
          ) {
            purpose
            state
            reservations(
              orderBy: [{attribute: ID, direction: ASC}]
            ) {
              id
            }
            subOrdersByPool(
              orderBy: [{attribute: INVENTORY_POOL_ID, direction: ASC}]
            ) {
              inventoryPool {
                id
              }
              reservations(
                orderBy: [{attribute: ID, direction: ASC}]
              ) {
                id
                status
              }
              state
            }
          }
        }
      GRAPHQL

      purpose = Faker::Lorem.sentence

      vars = {
        purpose: purpose
      }

      result = query(q, user.id, vars).deep_symbolize_keys
      expect(result[:data]).to eq({
        submitOrder: {
          purpose: purpose,
          state: ['SUBMITTED'],
          reservations: [
            { id: '100ffcc9-5401-415b-9185-5fffa8e5c526' },
            { id: '20fbda2e-9265-4728-8e70-418c2b348d8a' },
            { id: 'bf7080fb-2118-472e-8cff-50a51d648389' }
          ],
          subOrdersByPool: [
            { inventoryPool: { id: '4e2f1362-0891-4df7-b760-16a2a8d3373f' },
              reservations: [
                { id: '20fbda2e-9265-4728-8e70-418c2b348d8a',
                  status: 'SUBMITTED' },
                { id: 'bf7080fb-2118-472e-8cff-50a51d648389',
                  status: 'SUBMITTED' }
              ],
              state: 'SUBMITTED'
            },
            { inventoryPool: { id: '8633ce17-37da-4802-a377-66ca78291d0a' },
              reservations: [
                { id: '100ffcc9-5401-415b-9185-5fffa8e5c526',
                  status: 'SUBMITTED' }
              ],
              state: 'SUBMITTED'
            }
          ]
        }
      })
      expect(result[:errors]).to be_nil
    end

    it 'fails due to availability validation' do
      Settings.first.update(timeout_minutes: 1)

      2.times do
        model_3.add_item(
          FactoryBot.create(:item,
                            is_borrowable: true,
                            responsible: inventory_pool_1)
        )
      end

      2.times do
        FactoryBot.create(:reservation,
                          leihs_model: model_3,
                          inventory_pool: inventory_pool_1,
                          start_date: Date.tomorrow,
                          end_date: Date.tomorrow + 1.day,
                          user: user)
      end

      q = <<-GRAPHQL
        mutation(
          $purpose: NonEmptyText!
          $title: NonEmptyText!
        ) {
          submitOrder(
            purpose: $purpose
            title: $purpose
          ) {
            purpose
          }
        }
      GRAPHQL

      purpose = Faker::Lorem.sentence

      vars = {
        purpose: purpose
      }

      result = query(q, user.id, vars).deep_symbolize_keys
      expect(result[:errors].map { |e| e[:message] })
        .to include 'Some reserved quantities are not available anymore.'
    end
  end

  it 'get one' do
    model_1.add_item(
      FactoryBot.create(:item,
                        is_borrowable: true,
                        responsible: inventory_pool_1)
    )

    model_2.add_item(
      FactoryBot.create(:item,
                        is_borrowable: true,
                        responsible: inventory_pool_2)
    )

    purpose = Faker::Lorem.sentence

    order = nil
    database.transaction do
      order = FactoryBot.create(:order,
                                id: '84391a0b-2a55-43f9-bf6d-bb144a2aaf96',
                                user: user,
                                purpose: purpose)

      pool_order_1 = FactoryBot.create(:pool_order,
                                       order: order,
                                       inventory_pool: inventory_pool_1,
                                       user: user,
                                       state: 'submitted',
                                       purpose: purpose)

      FactoryBot.create(:reservation,
                        id: '02b76d67-30ad-41e3-b747-6b573178c85b',
                        leihs_model: model_1,
                        inventory_pool: inventory_pool_1,
                        order: pool_order_1,
                        status: 'submitted',
                        user: user)

      pool_order_2 = FactoryBot.create(:pool_order,
                                       order: order,
                                       inventory_pool: inventory_pool_2,
                                       user: user,
                                       state: 'approved',
                                       purpose: purpose)

      FactoryBot.create(:reservation,
                        id: '9be3e33c-5c56-4456-9f13-de948d06d8c4',
                        leihs_model: model_1,
                        inventory_pool: inventory_pool_2,
                        order: pool_order_2,
                        status: 'approved',
                        user: user)
    end

    q = <<-GRAPHQL
      query {
        order(id: "#{order.id}") {
          borrowUrl
          state
          subOrdersByPool {
            inventoryPool {
              id
              isActive
            }
          }
        }
      }
    GRAPHQL

    result = query(q, user.id).deep_symbolize_keys
    expect(result).to eq(
      { data:
       { order:
         { borrowUrl: "#{LEIHS_BORROW_HTTP_BASE_URL}/app/borrow/orders/#{order.id}",
           state: ["APPROVED", "SUBMITTED"],
           subOrdersByPool: [
             {inventoryPool: {id: inventory_pool_1.id, isActive: true}},
             {inventoryPool: {id: inventory_pool_2.id, isActive: false}}
           ]}}}
    )
    expect(result[:errors]).to be_nil
  end

  it 'filter according to states' do
    model_1.add_item(
      FactoryBot.create(:item,
                        is_borrowable: true,
                        responsible: inventory_pool_1)
    )

    model_2.add_item(
      FactoryBot.create(:item,
                        is_borrowable: true,
                        responsible: inventory_pool_2)
    )

    purpose = Faker::Lorem.sentence

    database.transaction do
      order = FactoryBot.create(:order,
                                id: 'bfc6a513-1e84-48df-b321-fe1b2eec9070',
                                user: user,
                                purpose: purpose)

      pool_order_1 = FactoryBot.create(:pool_order,
                                       order: order,
                                       inventory_pool: inventory_pool_1,
                                       user: user,
                                       state: 'approved',
                                       purpose: purpose)

      FactoryBot.create(:reservation,
                        id: 'b0be21cc-cd21-4e4e-a22e-6a21b03e6f38',
                        leihs_model: model_1,
                        inventory_pool: inventory_pool_1,
                        order: pool_order_1,
                        status: 'approved',
                        user: user)

      pool_order_2 = FactoryBot.create(:pool_order,
                                       order: order,
                                       inventory_pool: inventory_pool_2,
                                       user: user,
                                       state: 'rejected',
                                       purpose: purpose)

      FactoryBot.create(:reservation,
                        id: '8ef38e22-3f7f-48b4-8244-4194c70855fe',
                        leihs_model: model_1,
                        inventory_pool: inventory_pool_2,
                        order: pool_order_2,
                        status: 'rejected',
                        user: user)
    end

    database.transaction do
      order = FactoryBot.create(:order,
                                id: '84391a0b-2a55-43f9-bf6d-bb144a2aaf96',
                                user: user,
                                purpose: purpose)

      pool_order_1 = FactoryBot.create(:pool_order,
                                       order: order,
                                       inventory_pool: inventory_pool_1,
                                       user: user,
                                       state: 'submitted',
                                       purpose: purpose)

      FactoryBot.create(:reservation,
                        id: '02b76d67-30ad-41e3-b747-6b573178c85b',
                        leihs_model: model_1,
                        inventory_pool: inventory_pool_1,
                        order: pool_order_1,
                        status: 'submitted',
                        user: user)

      pool_order_2 = FactoryBot.create(:pool_order,
                                       order: order,
                                       inventory_pool: inventory_pool_2,
                                       user: user,
                                       state: 'approved',
                                       purpose: purpose)

      FactoryBot.create(:reservation,
                        id: '9be3e33c-5c56-4456-9f13-de948d06d8c4',
                        leihs_model: model_1,
                        inventory_pool: inventory_pool_2,
                        order: pool_order_2,
                        status: 'approved',
                        user: user)
    end

    q = <<-GRAPHQL
      query {
        orders(
          states: [APPROVED, SUBMITTED]
          orderBy: [{attribute: ID, direction: ASC}]
        ) {
          edges {
            node {
              id
              state
            }
          }
        }
      }
    GRAPHQL

    result = query(q, user.id).deep_symbolize_keys
    result.dig(:data, :orders, :edges).each do |o|
      o[:node][:state] = o[:node][:state].to_set
    end
    expect(result[:data]).to eq({
      orders: {
        edges: [
          { node: { id: '84391a0b-2a55-43f9-bf6d-bb144a2aaf96',
                    state: Set['SUBMITTED', 'APPROVED'] } }
        ]
      }
    })
    expect(result[:errors]).to be_nil
  end
end
