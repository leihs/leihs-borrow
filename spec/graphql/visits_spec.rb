require 'spec_helper'
require_relative 'graphql_helper'

describe 'visits' do
  let(:user) do
    FactoryBot.create(
      :user,
      id: 'f7e10f46-c5bb-4b31-bf64-6431f8bc7fcc'
    )
  end

  let(:inventory_pool_1) do
    FactoryBot.create(
      :inventory_pool,
      id: 'cfffa894-c0bd-42f3-b3d5-27603f00138d'
    )
  end

  let(:inventory_pool_2) do
    FactoryBot.create(
      :inventory_pool,
      id: '9ad7032a-cfe2-45a3-885e-24fb70444de1'
    )
  end

  let(:model_1) do
    m = FactoryBot.create(:leihs_model,
                          id: 'ec3840a3-2a09-48b2-a3b9-39a0809688f3')
    3.times do
      FactoryBot.create(:item,
                        leihs_model: m,
                        responsible: inventory_pool_1,
                        is_borrowable: true)
    end
    3.times do
      FactoryBot.create(:item,
                        leihs_model: m,
                        responsible: inventory_pool_2,
                        is_borrowable: true)
    end
    m
  end

  let(:model_2) do
    m = FactoryBot.create(:leihs_model,
                          id: '94e65bd4-3dae-48e2-8cda-ae404353bf9b')
    3.times do
      FactoryBot.create(:item,
                        leihs_model: m,
                        responsible: inventory_pool_1,
                        is_borrowable: true)
    end
    3.times do
      FactoryBot.create(:item,
                        leihs_model: m,
                        responsible: inventory_pool_2,
                        is_borrowable: true)
    end
    m
  end

  before(:example) do
    FactoryBot.create(:direct_access_right,
                      inventory_pool: inventory_pool_1,
                      user: user)

    FactoryBot.create(:direct_access_right,
                      inventory_pool: inventory_pool_2,
                      user: user)
  end

  it 'works' do
    purpose = Faker::Lorem.sentence

    database.transaction do
      order = FactoryBot.create(:order,
                                user: user,
                                purpose: purpose)

      pool_order_1 = FactoryBot.create(:pool_order,
                                       order: order,
                                       inventory_pool: inventory_pool_1,
                                       user: user,
                                       state: 'approved',
                                       purpose: purpose)

      pool_order_2 = FactoryBot.create(:pool_order,
                                       order: order,
                                       inventory_pool: inventory_pool_2,
                                       user: user,
                                       state: 'submitted',
                                       purpose: purpose)

      FactoryBot.create(:reservation,
                        id: '56b01674-26f0-4e5e-ac61-7625af86520d',
                        leihs_model: model_1,
                        inventory_pool: inventory_pool_1,
                        order: pool_order_1,
                        status: 'approved',
                        start_date: (Date.today + 1.day).to_s,
                        end_date: (Date.today + 2.days).to_s,
                        user: user)

      FactoryBot.create(:reservation,
                        id: '4f92b4ef-ba21-4a7d-9d4d-622e7bddf688',
                        leihs_model: model_1,
                        inventory_pool: inventory_pool_1,
                        order: pool_order_1,
                        status: 'approved',
                        start_date: (Date.today + 1.day).to_s,
                        end_date: (Date.today + 2.days).to_s,
                        user: user)

      FactoryBot.create(:reservation,
                        id: '7875f990-10eb-4edd-af0e-9ab11533bd3d',
                        leihs_model: model_1,
                        inventory_pool: inventory_pool_2,
                        order: pool_order_2,
                        status: 'submitted',
                        start_date: (Date.today + 3.day).to_s,
                        end_date: (Date.today + 4.days).to_s,
                        user: user)
    end

    c_id = '8051e761-0ad9-4fbf-9bb3-726f66dc0555'
    Contract.create_with_disabled_triggers(c_id,
                                           user.id,
                                           inventory_pool_1.id)

    database.transaction do
      order = FactoryBot.create(:order,
                                user: user,
                                purpose: purpose)

      pool_order_1 = FactoryBot.create(:pool_order,
                                       order: order,
                                       inventory_pool: inventory_pool_1,
                                       user: user,
                                       state: 'approved',
                                       purpose: purpose)

      FactoryBot.create(:reservation,
                        id: '9aab0840-d673-4255-970a-4b5b3136b5a4',
                        leihs_model: model_1,
                        inventory_pool: inventory_pool_1,
                        order: pool_order_1,
                        contract_id: c_id,
                        status: 'signed',
                        start_date: (Date.today + 1.day).to_s,
                        end_date: (Date.today + 2.days).to_s,
                        user: user)
    end

    q = <<-GRAPHQL
      query {
        pickups(orderBy: [{attribute: DATE, direction: ASC}]) {
          date
          inventoryPool {
            id
          }
          quantity
          reservations(orderBy: [{attribute: ID, direction: ASC}]) {
            id
          }
        }
        returns {
          date
          inventoryPool {
            id
          }
          quantity
          reservations(orderBy: [{attribute: ID, direction: ASC}]) {
            id
          }
        }
      }
    GRAPHQL

    result = query(q, user.id)
    expect_graphql_result(result, {
      :pickups => [
        {:date => "#{(Date.today + 1.day).to_s}T00:00:00Z",
         :inventoryPool => {:id => "cfffa894-c0bd-42f3-b3d5-27603f00138d"},
         :quantity => 2,
         :reservations => [{:id => "4f92b4ef-ba21-4a7d-9d4d-622e7bddf688"},
                           {:id => "56b01674-26f0-4e5e-ac61-7625af86520d"}]}
      ],
      :returns => [
        {:date => "#{(Date.today + 2.day).to_s}T00:00:00Z",
         :inventoryPool => {:id => "cfffa894-c0bd-42f3-b3d5-27603f00138d"},
         :quantity => 1,
         :reservations => [{:id => "9aab0840-d673-4255-970a-4b5b3136b5a4"}]}
      ]
    })
  end
end
