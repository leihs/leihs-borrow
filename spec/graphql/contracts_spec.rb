require "spec_helper"
require_relative "graphql_helper"

describe "contracts" do
  let(:user) do
    FactoryBot.create(
      :user,
      id: "7efc29da-c808-4661-83c0-a7caa8c1d681"
    )
  end

  let(:inventory_pool) do
    FactoryBot.create(
      :inventory_pool,
      id: "b6107e70-6f85-4465-984c-a96a9ee1fa97"
    )
  end

  let(:inventory_pool_2) do
    FactoryBot.create(
      :inventory_pool,
      id: "eccdb633-07c1-4242-b801-685978884655"
    )
  end

  let(:model) do
    FactoryBot.create(:leihs_model,
      id: "85b6fded-3370-455e-a4fd-60d8ace298ee")
  end

  before(:example) do
    FactoryBot.create(:direct_access_right,
      inventory_pool: inventory_pool,
      user: user)
  end

  it "connection" do
    model.add_item(
      FactoryBot.create(:item,
        is_borrowable: true,
        responsible: inventory_pool)
    )

    c1_id = "e1828e88-dec8-4430-963f-2a528aeb7d60"
    Contract.create_with_disabled_triggers(c1_id,
      user.id,
      inventory_pool.id)

    FactoryBot.create(:reservation,
      leihs_model: model,
      inventory_pool: inventory_pool,
      status: "signed",
      contract_id: c1_id,
      user: user)

    c2_id = "7f484dff-8058-4cdf-b91d-77ae5c459d0e"
    Contract.create_with_disabled_triggers(c2_id,
      user.id,
      inventory_pool.id)

    FactoryBot.create(:reservation,
      leihs_model: model,
      inventory_pool: inventory_pool,
      status: "signed",
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
          {node: {id: "7f484dff-8058-4cdf-b91d-77ae5c459d0e"}},
          {node: {id: "e1828e88-dec8-4430-963f-2a528aeb7d60"}}
        ]
      }
    })
  end

  context "from order through pool orders down to reservations" do
    let(:order) do
      FactoryBot.create(:order, user: user)
    end

    let(:pool_order_1) do
      FactoryBot.create(:pool_order,
        id: "81c2d71b-e44f-461a-88a8-cabdda2946f6",
        user: user,
        inventory_pool: inventory_pool,
        state: :approved,
        order: order)
    end

    let(:pool_order_2) do
      FactoryBot.create(:pool_order,
        id: "f21b2729-a0e0-48ec-af96-feb2a7d45121",
        user: user,
        inventory_pool: inventory_pool_2,
        state: :approved,
        order: order)
    end

    let(:model_2) do
      FactoryBot.create(:leihs_model)
    end

    let(:model_3) do
      FactoryBot.create(:leihs_model)
    end

    let(:model_4) do
      FactoryBot.create(:leihs_model)
    end

    it "works" do
      database.transaction do
        c1_id = "aadc0513-daeb-40a7-b566-2b41b005f2e0"
        Contract.create_with_disabled_triggers(c1_id,
          user.id,
          inventory_pool.id)
        c2_id = "d0954b93-af72-46ba-96c0-6058255e031e"
        Contract.create_with_disabled_triggers(c2_id,
          user.id,
          inventory_pool.id)
        c3_id = "d4c80a2e-cd06-445f-9520-a7ce1adaf8f4"
        Contract.create_with_disabled_triggers(c3_id,
          user.id,
          inventory_pool_2.id)

        r1 = FactoryBot.create(:reservation,
          id: "0c1bbc9f-2b3e-44f9-b0bb-69919200e571",
          user: user,
          inventory_pool: inventory_pool,
          leihs_model: model,
          item_id: FactoryBot.create(:item,
            leihs_model: model,
            responsible: inventory_pool,
            owner: inventory_pool).id,
          status: :signed,
          order: pool_order_1,
          contract_id: c1_id)

        r2 = FactoryBot.create(:reservation,
          id: "8a215e9d-6016-42ca-812d-249b04e1e7ae",
          user: user,
          inventory_pool: inventory_pool,
          leihs_model: model,
          item_id: FactoryBot.create(:item,
            leihs_model: model_2,
            responsible: inventory_pool,
            owner: inventory_pool).id,
          status: :signed,
          order: pool_order_1,
          contract_id: c1_id)

        r3 = FactoryBot.create(:reservation,
          id: "b6271cff-21ab-445c-a74d-fcd8965789dc",
          user: user,
          inventory_pool: inventory_pool,
          leihs_model: model,
          item_id: FactoryBot.create(:item,
            leihs_model: model_3,
            responsible: inventory_pool,
            owner: inventory_pool).id,
          status: :signed,
          order: pool_order_1,
          contract_id: c2_id)

        r4 = FactoryBot.create(:reservation,
          id: "f0d86df9-8f2c-4b30-b6ff-a63e65e1cf9b",
          user: user,
          inventory_pool: inventory_pool_2,
          leihs_model: model,
          item_id: FactoryBot.create(:item,
            leihs_model: model_4,
            responsible: inventory_pool_2,
            owner: inventory_pool_2).id,
          status: :signed,
          order: pool_order_2,
          contract_id: c3_id)

        q = <<~QUERY
          {
            rental(id: "#{order.id}") {
              contracts(orderBy: [{attribute: ID, direction: ASC}]) {
                edges {
                  node {
                    id
                    reservations(orderBy: [{attribute: ID, direction: ASC}]) {
                      id
                      contract {
                        id
                      }
                    }
                  }
                }
              }
              subOrdersByPool(orderBy: [{attribute: ID, direction: ASC}]) {
                id
                reservations(orderBy: [{attribute: ID, direction: ASC}]) {
                  id
                  contract {
                    id
                  }
                }
                contracts(orderBy: [{attribute: ID, direction: ASC}]) {
                  edges {
                    node {
                      id
                      reservations(orderBy: [{attribute: ID, direction: ASC}]) {
                        id
                        contract {
                          id
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        QUERY

        result = query(q, user.id)
        expect_graphql_result(result, {
          rental: {
            contracts: {
              edges: [
                {node: {
                  id: c1_id,
                  reservations: [
                    {id: r1.id,
                     contract: {id: c1_id}},
                    {id: r2.id,
                     contract: {id: c1_id}}
                  ]
                }},
                {node: {
                  id: c2_id,
                  reservations: [
                    {id: r3.id,
                     contract: {id: c2_id}}
                  ]
                }},
                {node: {
                  id: c3_id,
                  reservations: [
                    {id: r4.id,
                     contract: {id: c3_id}}
                  ]
                }}
              ]
            },
            subOrdersByPool: [
              {id: pool_order_1.id,
               reservations: [
                 {id: r1.id,
                  contract: {id: c1_id}},
                 {id: r2.id,
                  contract: {id: c1_id}},
                 {id: r3.id,
                  contract: {id: c2_id}}
               ],
               contracts: {
                 edges: [
                   {node: {
                     id: c1_id,
                     reservations: [
                       {id: r1.id,
                        contract: {id: c1_id}},
                       {id: r2.id,
                        contract: {id: c1_id}}
                     ]
                   }},
                   {node: {
                     id: c2_id,
                     reservations: [
                       {id: r3.id,
                        contract: {id: c2_id}}
                     ]
                   }}
                 ]
               }},
              {id: pool_order_2.id,
               reservations: [
                 {id: r4.id,
                  contract: {id: c3_id}}
               ],
               contracts: {
                 edges: [
                   {node: {
                     id: c3_id,
                     reservations: [
                       {id: r4.id,
                        contract: {id: c3_id}}
                     ]
                   }}
                 ]
               }}
            ]
          }
        })
      end
    end
  end
end
