require 'spec_helper'
require_relative 'graphql_helper'

describe 'currentUser' do
  it 'works' do
    pool_A = FactoryBot.create(:inventory_pool,
                               id: 'de1ab6c2-5c85-45fb-aebf-527b6096411c',
                               name: 'Pool A (customer)')
    FactoryBot.create(:inventory_pool,
                      id: 'b94b417c-2b7e-45de-af9e-9ce6718ac84d',
                      name: 'Pool B (customer)')
    FactoryBot.create(:inventory_pool,
                      id: '07bcbc06-89ae-44c6-bf7a-ceb5cd8a853a',
                      name: 'Pool C (lending manager)')
    FactoryBot.create(:inventory_pool,
                      name: 'Pool D (no access right)')
    FactoryBot.create(:inventory_pool,
                      id: 'f2582328-2363-450d-a002-3858df315856',
                      name: 'Pool E (deleted access right)')
    FactoryBot.create(:inventory_pool,
                      id: 'c52b6ec9-f213-42e6-8273-b2efa71360c0',
                      name: 'Pool F (inactive)',
                      is_active: false)
    user = FactoryBot.create(
      :user,
      id: '0567f6b0-540c-4619-9251-9ea099a5d50d',
      access_rights: [
        FactoryBot.create(:access_right,
                          role: :customer,
                          inventory_pool_id: '07bcbc06-89ae-44c6-bf7a-ceb5cd8a853a'),
        FactoryBot.create(:access_right,
                          role: :customer,
                          inventory_pool_id: 'b94b417c-2b7e-45de-af9e-9ce6718ac84d'),
        FactoryBot.create(:access_right,
                          role: :lending_manager,
                          inventory_pool_id: 'de1ab6c2-5c85-45fb-aebf-527b6096411c'),
        FactoryBot.create(:access_right,
                          role: :customer,
                          deleted_at: Date.yesterday,
                          inventory_pool_id: 'de1ab6c2-5c85-45fb-aebf-527b6096411c'),
        FactoryBot.create(:access_right,
                          role: :customer,
                          inventory_pool_id: 'c52b6ec9-f213-42e6-8273-b2efa71360c0')
      ]
    )

    FactoryBot.create(:reservation,
                      id: '770632c4-f268-4ed6-bcc0-c8bc032bc9b5',
                      status: 'unsubmitted',
                      user: user,
                      inventory_pool: pool_A,
                      start_date: Date.tomorrow,
                      end_date: Date.tomorrow + 5.days)

    model_1 = FactoryBot.create(:leihs_model, product: 'Model A')
    model_2 = FactoryBot.create(:leihs_model, product: 'Model B')

    model_1.add_item(
      FactoryBot.create(:item, responsible: pool_A, is_borrowable: true)
    )

    FactoryBot.create(:favorite_model, leihs_model: model_1, user: user)
    FactoryBot.create(:favorite_model, leihs_model: model_2, user: user)

    q = <<-GRAPHQL
      query Query {
        currentUser {
          user {
            id
          }
          inventoryPools(orderBy: [{attribute: NAME, direction: ASC}]) {
            name
          }
          unsubmittedOrder {
            validUntil
            reservations {
              id
              updatedAt
            }
          }
          favoriteModels {
            totalCount
            edges {
              node {
                name
                isReservable
              }
            }
          }
        }
      }
    GRAPHQL


    result = query(q, '0567f6b0-540c-4619-9251-9ea099a5d50d')
    timestamp = \
      result
      .dig(:data, :currentUser, :unsubmittedOrder, :reservations)
      .first[:updatedAt]
    expect_graphql_result(result, {
      currentUser: {
        user: {
          id: '0567f6b0-540c-4619-9251-9ea099a5d50d'
        },
        inventoryPools: [
          { name: 'Pool A (customer)' },
          { name: 'Pool B (customer)' },
          { name: 'Pool C (lending manager)' }
        ],
        unsubmittedOrder: {
          validUntil: timestamp,
          reservations: [
            { id: '770632c4-f268-4ed6-bcc0-c8bc032bc9b5',
              updatedAt: timestamp }
          ]
        },
        favoriteModels: {
          totalCount: 2,
          edges: [
            { node: { name: 'Model A', isReservable: true } },
            { node: { name: 'Model B', isReservable: false } }
          ]
        }
      }
    })
  end
end
