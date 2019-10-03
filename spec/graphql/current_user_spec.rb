require 'spec_helper'
require_relative 'graphql_helper'

describe 'currentUser' do
  it 'works' do
    data = [
      { factory: :inventory_pool,
        id: 'de1ab6c2-5c85-45fb-aebf-527b6096411c',
        name: 'Pool A (customer)'
      },
      { factory: :inventory_pool,
        id: 'b94b417c-2b7e-45de-af9e-9ce6718ac84d',
        name: 'Pool B (customer)'
      },
      { factory: :inventory_pool,
        id: '07bcbc06-89ae-44c6-bf7a-ceb5cd8a853a',
        name: 'Pool C (lending manager)'
      },
      { factory: :inventory_pool,
        name: 'Pool D (no access right)'
      },
      { factory: :inventory_pool,
        id: 'f2582328-2363-450d-a002-3858df315856',
        name: 'Pool E (deleted access right)'
      },
      { factory: :inventory_pool,
        id: 'c52b6ec9-f213-42e6-8273-b2efa71360c0',
        name: 'Pool F (inactive)',
        is_active: false
      },
      { factory: :user,
        id: '0567f6b0-540c-4619-9251-9ea099a5d50d',
        access_rights: [
          { factory: :access_right,
            role: :customer,
            inventory_pool_id: '07bcbc06-89ae-44c6-bf7a-ceb5cd8a853a' },
          { factory: :access_right,
            role: :customer,
            inventory_pool_id: 'b94b417c-2b7e-45de-af9e-9ce6718ac84d' },
          { factory: :access_right,
            role: :lending_manager,
            inventory_pool_id: 'de1ab6c2-5c85-45fb-aebf-527b6096411c' },
          { factory: :access_right,
            role: :customer,
            deleted_at: Date.yesterday,
            inventory_pool_id: 'de1ab6c2-5c85-45fb-aebf-527b6096411c' },
          { factory: :access_right,
            role: :customer,
            inventory_pool_id: 'c52b6ec9-f213-42e6-8273-b2efa71360c0' }
        ]
      }
    ]

    factorize!(data)

    q = <<-GRAPHQL
      query Query {
        currentUser {
          user {
            id
          }
          inventoryPools(orderBy: [{attribute: NAME, direction: ASC}]) {
            name
          }
        }
      }
    GRAPHQL

    result = query(q, '0567f6b0-540c-4619-9251-9ea099a5d50d')

    expect(result['data']).to eq({
      'currentUser' => {
        'user' => {
          'id' => '0567f6b0-540c-4619-9251-9ea099a5d50d'
        },
        'inventoryPools' => [
          { 'name' => 'Pool A (customer)' },
          { 'name' => 'Pool B (customer)' },
          { 'name' => 'Pool C (lending manager)' }
        ]
      }
    })

    expect(result).not_to include(:errors)
  end
end
