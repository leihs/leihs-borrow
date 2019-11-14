require 'spec_helper'
require_relative 'graphql_helper'

describe 'contracts' do
  let(:user) do
    FactoryBot.create(
      :user,
      id: '7efc29da-c808-4661-83c0-a7caa8c1d681'
    )
  end

  let(:inventory_pool) do
    FactoryBot.create(
      :inventory_pool,
      id: 'b6107e70-6f85-4465-984c-a96a9ee1fa97'
    )
  end

  let(:model) do
    FactoryBot.create(:leihs_model,
                      id: '85b6fded-3370-455e-a4fd-60d8ace298ee')
  end

  before(:example) do
    FactoryBot.create(:access_right,
                      inventory_pool: inventory_pool,
                      user: user)
  end

  it 'connection' do
    model.add_item(
      FactoryBot.create(:item,
                        is_borrowable: true,
                        responsible: inventory_pool)
    )

    c1_id = 'e1828e88-dec8-4430-963f-2a528aeb7d60'

    database.run <<-SQL
      SET session_replication_role = REPLICA;

      INSERT INTO contracts(
        id,
        user_id,
        inventory_pool_id,
        compact_id,
        purpose,
        created_at,
        updated_at,
        state
      )
      VALUES (
        '#{c1_id}',
        '#{user.id}',
        '#{inventory_pool.id}',
        '#{Faker::Lorem.word}',
        '#{Faker::Lorem.sentence}',
        now(),
        now(),
        'open'
        );

      SET session_replication_role = DEFAULT;
    SQL

    r1 = FactoryBot.create(:reservation,
                           leihs_model: model,
                           inventory_pool: inventory_pool,
                           status: 'signed',
                           contract_id: c1_id,
                           user: user)

    c2_id = '7f484dff-8058-4cdf-b91d-77ae5c459d0e'

    database.run <<-SQL
      SET session_replication_role = REPLICA;

      INSERT INTO contracts(
        id,
        user_id,
        inventory_pool_id,
        compact_id,
        purpose,
        created_at,
        updated_at,
        state
      )
      VALUES (
        '#{c2_id}',
        '#{user.id}',
        '#{inventory_pool.id}',
        '#{Faker::Lorem.word}',
        '#{Faker::Lorem.sentence}',
        now(),
        now(),
        'open'
        );

      SET session_replication_role = DEFAULT;
    SQL

    r2 = FactoryBot.create(:reservation,
                           leihs_model: model,
                           inventory_pool: inventory_pool,
                           status: 'signed',
                           contract_id: c1_id,
                           user: user)

    q = <<-GRAPHQL
      query {
        contracts(orderBy: [{attribute: ID, direction: ASC}]) {
          edges {
            node {
              id
            }
          }
        }
      }
    GRAPHQL

    result = query(q, user.id)
    expect_graphql_result(result, {
      contracts: {
        edges: [
          { node: { id: '7f484dff-8058-4cdf-b91d-77ae5c459d0e' }},
          { node: { id: 'e1828e88-dec8-4430-963f-2a528aeb7d60' }}
        ]
      }
    })
  end
end
