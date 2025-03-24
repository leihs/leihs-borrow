require "spec_helper"
require_relative "graphql_helper"
require_relative "models/reservation_advance_days_context"

def cursor(uuid)
  database[
    "SELECT encode(decode(replace('#{uuid}', '-', ''), 'hex'), 'base64') AS result"
  ].first[:result]
end

describe "models connection" do
  before :each do
    @user = FactoryBot.create(
      :user,
      id: "ce3665a0-2711-44b8-aa47-11fb881c3f91"
    )
    @inventory_pool = FactoryBot.create(
      :inventory_pool,
      id: "8f613f14-3b6d-4d5c-9804-913e2da1109e"
    )
    FactoryBot.create(:direct_access_right,
      inventory_pool: @inventory_pool,
      user: @user)

    @model_1 = FactoryBot.create(
      :leihs_model,
      id: "0cad263d-14b9-4595-9878-7adde7f4f586"
    )
    @model_2 = FactoryBot.create(
      :leihs_model,
      id: "1adfe883-3546-4b5c-9ed6-b18f01f77723"
    )
    @model_3 = FactoryBot.create(
      :leihs_model,
      id: "29c1bdf9-7764-4e1e-bf9e-902f908be8d5"
    )

    LeihsModel.all.map do |model|
      FactoryBot.create(:item,
        leihs_model: model,
        responsible: @inventory_pool,
        is_borrowable: true)
    end
  end

  it "get each successively" do
    # get all without `first` and `after`
    q = <<-GRAPHQL
      {
        models(orderBy: [{attribute: ID, direction: ASC}]) {
          totalCount
          edges {
            node {
              id
            }
          }
          pageInfo {
            hasNextPage
          }
        }
      }
    GRAPHQL

    @result = query(q, @user.id).deep_symbolize_keys
    expect(@result).not_to include(:errors)

    expect(@result[:data]).to eq({
      models: {
        totalCount: 3,
        edges: [
          {node: {
            id: @model_1.id
          }},
          {node: {
            id: @model_2.id
          }},
          {node: {
            id: @model_3.id
          }}
        ],
        pageInfo: {
          hasNextPage: false
        }
      }
    })

    # get first one without `after`
    q = <<-GRAPHQL
      {
        models(
          first: 1,
          orderBy: [{attribute: ID, direction: ASC}]
        ) {
          totalCount
          edges {
            node {
              id
            }
          }
          pageInfo {
            endCursor
            hasNextPage
          }
        }
      }
    GRAPHQL

    @result = query(q, @user.id).deep_symbolize_keys
    expect(@result).not_to include(:errors)
    ec = @result.delete_in!(:data, :models, :pageInfo, :endCursor)

    expect(@result[:data]).to eq({
      models: {
        totalCount: 3,
        edges: [
          {node: {
            id: @model_1.id
          }}
        ],
        pageInfo: {
          hasNextPage: true
        }
      }
    })

    # get second one with `after` and `first`
    q = <<-GRAPHQL
      {
        models(
          first: 1,
          after: "#{ec}",
          orderBy: [{attribute: ID, direction: ASC}]
        ) {
          totalCount
          edges {
            node {
              id
            }
          }
          pageInfo {
            endCursor
            hasNextPage
          }
        }
      }
    GRAPHQL

    @result = query(q, @user.id).deep_symbolize_keys
    expect(@result).not_to include(:errors)
    ec = @result.delete_in!(:data, :models, :pageInfo, :endCursor)

    expect(@result[:data]).to eq({
      models: {
        totalCount: 3,
        edges: [
          {node: {
            id: @model_2.id
          }}
        ],
        pageInfo: {
          hasNextPage: true
        }
      }
    })

    # get third one with `after` and `first`
    q = <<-GRAPHQL
      {
        models(
          first: 1,
          after: "#{ec}",
          orderBy: [{attribute: ID, direction: ASC}]
        ) {
          totalCount
          edges {
            node {
              id
            }
          }
          pageInfo {
            endCursor
            hasNextPage
          }
        }
      }
    GRAPHQL

    @result = query(q, @user.id).deep_symbolize_keys
    expect(@result).not_to include(:errors)
    ec = @result.delete_in!(:data, :models, :pageInfo, :endCursor)

    expect(@result[:data]).to eq({
      models: {
        totalCount: 3,
        edges: [
          {node: {
            id: @model_3.id
          }}
        ],
        pageInfo: {
          hasNextPage: false
        }
      }
    })

    # go beyond what's available
    q = <<-GRAPHQL
      {
        models(
          first: 1,
          after: "#{ec}",
          orderBy: [{attribute: ID, direction: ASC}]
        ) {
          totalCount
          edges {
            node {
              id
            }
          }
          pageInfo {
            endCursor
            hasNextPage
          }
        }
      }
    GRAPHQL

    @result = query(q, @user.id).deep_symbolize_keys
    expect(@result).not_to include(:errors)

    expect(@result[:data]).to eq({
      models: {
        totalCount: 3,
        edges: [],
        pageInfo: {
          endCursor: nil,
          hasNextPage: false
        }
      }
    })
  end

  it "get 1st 2" do
    q = <<-GRAPHQL
      {
        models(first: 2, orderBy: [{attribute: ID, direction: ASC}]) {
          totalCount
          edges {
            node {
              id
            }
          }
          pageInfo {
            hasNextPage
          }
        }
      }
    GRAPHQL

    @result = query(q, @user.id).deep_symbolize_keys
    expect(@result).not_to include(:errors)

    expect(@result[:data]).to eq({
      models: {
        totalCount: 3,
        edges: [
          {node: {
            id: @model_1.id
          }},
          {node: {
            id: @model_2.id
          }}
        ],
        pageInfo: {
          hasNextPage: true
        }
      }
    })
  end

  it "get all after 1st" do
    q = <<-GRAPHQL
      {
        models(
          after: "#{cursor(@model_1.id)}",
          orderBy: [{attribute: ID, direction: ASC}]
        ) {
          totalCount
          edges {
            node {
              id
            }
          }
          pageInfo {
            hasNextPage
          }
        }
      }
    GRAPHQL

    @result = query(q, @user.id).deep_symbolize_keys
    expect(@result).not_to include(:errors)

    expect(@result[:data]).to eq({
      models: {
        totalCount: 3,
        edges: [
          {node: {
            id: @model_2.id
          }},
          {node: {
            id: @model_3.id
          }}
        ],
        pageInfo: {
          hasNextPage: false
        }
      }
    })
  end

  context "nodes modifications" do
    it "delete a row before the cursor" do
      q = <<-GRAPHQL
        {
          models(
            first: 2,
            orderBy: [{attribute: ID, direction: ASC}]
          ) {
            totalCount
            edges {
              node {
                id
              }
            }
            pageInfo {
              endCursor
              hasNextPage
            }
          }
        }
      GRAPHQL

      @result = query(q, @user.id).deep_symbolize_keys
      ec = @result.delete_in!(:data, :models, :pageInfo, :endCursor)

      db_with_disabled_triggers do
        database.run <<~SQL
          DELETE FROM items WHERE model_id = '#{@model_1.id}';
        SQL
      end
      @model_1.delete

      q = <<-GRAPHQL
        {
          models(
            after: "#{ec}",
            orderBy: [{attribute: ID, direction: ASC}]
          ) {
            totalCount
            edges {
              node {
                id
              }
            }
            pageInfo {
              hasNextPage
            }
          }
        }
      GRAPHQL

      @result = query(q, @user.id).deep_symbolize_keys

      expect(@result[:data]).to eq({
        models: {
          totalCount: 2,
          edges: [
            {node: {
              id: @model_3.id
            }}
          ],
          pageInfo: {
            hasNextPage: false
          }
        }
      })
    end

    it "delete a row after the cursor" do
      q = <<-GRAPHQL
        {
          models(
            first: 1,
            orderBy: [{attribute: ID, direction: ASC}]
          ) {
            totalCount
            edges {
              node {
                id
              }
            }
            pageInfo {
              endCursor
              hasNextPage
            }
          }
        }
      GRAPHQL

      @result = query(q, @user.id).deep_symbolize_keys
      ec = @result.delete_in!(:data, :models, :pageInfo, :endCursor)

      db_with_disabled_triggers do
        database.run <<~SQL
          DELETE FROM items WHERE model_id = '#{@model_2.id}';
        SQL
      end
      @model_2.delete

      q = <<-GRAPHQL
        {
          models(
            after: "#{ec}",
            orderBy: [{attribute: ID, direction: ASC}]
          ) {
            totalCount
            edges {
              node {
                id
              }
            }
            pageInfo {
              hasNextPage
            }
          }
        }
      GRAPHQL

      @result = query(q, @user.id).deep_symbolize_keys

      expect(@result[:data]).to eq({
        models: {
          totalCount: 2,
          edges: [
            {node: {
              id: @model_3.id
            }}
          ],
          pageInfo: {
            hasNextPage: false
          }
        }
      })
    end

    it "delete the cursor" do
      q = <<-GRAPHQL
        {
          models(
            first: 1,
            orderBy: [{attribute: ID, direction: ASC}]
          ) {
            totalCount
            edges {
              node {
                id
              }
            }
            pageInfo {
              endCursor
              hasNextPage
            }
          }
        }
      GRAPHQL

      @result = query(q, @user.id).deep_symbolize_keys
      ec = @result.delete_in!(:data, :models, :pageInfo, :endCursor)

      db_with_disabled_triggers do
        database.run <<~SQL
          DELETE FROM items WHERE model_id = '#{@model_1.id}';
        SQL
      end
      @model_1.delete

      q = <<-GRAPHQL
        {
          models(
            after: "#{ec}",
            orderBy: [{attribute: ID, direction: ASC}]
          ) {
            totalCount
            edges {
              node {
                id
              }
            }
            pageInfo {
              hasNextPage
            }
          }
        }
      GRAPHQL

      @result = query(q, @user.id).deep_symbolize_keys

      expect(@result.dig(:data, :models)).to be_nil
      expect(@result[:errors].first[:message])
        .to eq "After cursor row does not exist!"
    end

    it "insert a row before the cursor" do
      q = <<-GRAPHQL
        {
          models(
            first: 1,
            orderBy: [{attribute: ID, direction: ASC}]
          ) {
            totalCount
            edges {
              node {
                id
              }
            }
            pageInfo {
              endCursor
              hasNextPage
            }
          }
        }
      GRAPHQL

      @result = query(q, @user.id).deep_symbolize_keys
      ec = @result.delete_in!(:data, :models, :pageInfo, :endCursor)

      @model_0 = FactoryBot.create(
        :leihs_model,
        id: "0a5a8688-fc09-4d36-b4f1-a6b25942a14d"
      )

      FactoryBot.create(:item,
        leihs_model: @model_0,
        responsible: @inventory_pool,
        is_borrowable: true)

      q = <<-GRAPHQL
        {
          models(
            after: "#{ec}",
            orderBy: [{attribute: ID, direction: ASC}]
          ) {
            totalCount
            edges {
              node {
                id
              }
            }
            pageInfo {
              hasNextPage
            }
          }
        }
      GRAPHQL

      @result = query(q, @user.id).deep_symbolize_keys

      expect(@result[:data]).to eq({
        models: {
          totalCount: 4,
          edges: [
            {node: {
              id: @model_2.id
            }},
            {node: {
              id: @model_3.id
            }}
          ],
          pageInfo: {
            hasNextPage: false
          }
        }
      })
    end

    it "insert a row after the cursor" do
      q = <<-GRAPHQL
        {
          models(
            first: 1,
            orderBy: [{attribute: ID, direction: ASC}]
          ) {
            totalCount
            edges {
              node {
                id
              }
            }
            pageInfo {
              endCursor
              hasNextPage
            }
          }
        }
      GRAPHQL

      @result = query(q, @user.id).deep_symbolize_keys
      ec = @result.delete_in!(:data, :models, :pageInfo, :endCursor)

      @model_4 = FactoryBot.create(
        :leihs_model,
        id: "30679ee8-4af7-4e02-82a6-8bc260686558"
      )

      FactoryBot.create(:item,
        leihs_model: @model_4,
        responsible: @inventory_pool,
        is_borrowable: true)

      q = <<-GRAPHQL
        {
          models(
            after: "#{ec}",
            orderBy: [{attribute: ID, direction: ASC}]
          ) {
            totalCount
            edges {
              node {
                id
              }
            }
            pageInfo {
              hasNextPage
            }
          }
        }
      GRAPHQL

      @result = query(q, @user.id).deep_symbolize_keys

      expect(@result[:data]).to eq({
        models: {
          totalCount: 4,
          edges: [
            {node: {
              id: @model_2.id
            }},
            {node: {
              id: @model_3.id
            }},
            {node: {
              id: @model_4.id
            }}
          ],
          pageInfo: {
            hasNextPage: false
          }
        }
      })
    end
  end

  it "associated entitites" do
    user = FactoryBot.create(:user,
      id: "4dc6adb4-ed7c-46cb-8573-8e585f70f4de")
    inventory_pool = FactoryBot.create(:inventory_pool,
      id: "232547a5-5f43-450c-896a-b692275a04ea")
    FactoryBot.create(:direct_access_right,
      inventory_pool: inventory_pool,
      user: user)

    recommend = FactoryBot.create(
      :leihs_model,
      id: "210a4116-162f-4947-bcb0-2d7d1a5c7b1c",
      items: [
        FactoryBot.create(
          :item,
          responsible: inventory_pool,
          is_borrowable: true
        )
      ]
    )

    model = FactoryBot.create(
      :leihs_model,
      id: "2bc1deb5-9428-4178-afd0-c06bb8d31ff3",
      items: [
        FactoryBot.create(
          :item,
          responsible: inventory_pool,
          is_borrowable: true
        )
      ],
      properties: [
        FactoryBot.create(
          :property,
          id: "2df736a4-825c-4f36-b48a-75875b3a3c26"
        )
      ],
      images: [
        FactoryBot.create(
          :image,
          :for_leihs_model,
          id: "7484b5d2-376a-4b15-8db0-54cc6bab02ea"
        )
      ],
      recommends: [recommend]
    )

    attachment =
      FactoryBot.build(:attachment,
        id: "919fbdd1-111c-49b7-aeb0-2d5d8825ed00")
    model.add_attachment(attachment)

    q = <<-GRAPHQL
      {
        models(
          orderBy: [{attribute: ID, direction: ASC}]
        ) {
          edges {
            node {
              id
              images {
                imageUrl
              }
              attachments {
                contentType
                attachmentUrl
              }
              properties {
                id
              }
              recommends {
                edges {
                  node {
                    id
                  }
                }
              }
            }
          }
        }
      }
    GRAPHQL

    result = query(q, user.id)
    expect_graphql_result(
      result,
      {models: {
        edges: [
          # recommend
          {node: {
            id: "210a4116-162f-4947-bcb0-2d7d1a5c7b1c",
            images: [],
            attachments: [],
            properties: [],
            recommends: {edges: []}
          }},
          # model of interest
          {node: {
            id: "2bc1deb5-9428-4178-afd0-c06bb8d31ff3",
            images: [{imageUrl: "/borrow/images/7484b5d2-376a-4b15-8db0-54cc6bab02ea"}],
            attachments: [{contentType: "application/pdf",
                           attachmentUrl: "/borrow/attachments/919fbdd1-111c-49b7-aeb0-2d5d8825ed00/#{attachment.filename}"}],
            properties: [{id: "2df736a4-825c-4f36-b48a-75875b3a3c26"}],
            recommends: {
              edges: [
                {node: {
                  id: "210a4116-162f-4947-bcb0-2d7d1a5c7b1c"
                }}
              ]
            }
          }}
        ]
      }}
    )
  end

  it "raises error if auth user not part of delegation" do
    @delegation = FactoryBot.create(:delegation)

    q = <<-GRAPHQL
      {
        models(
          first: 1,
          userId: "#{@delegation.id}"
        ) {
          totalCount
        }
      }
    GRAPHQL

    @result = query(q, @user.id).deep_symbolize_keys

    expect(@result.dig(:data, :models)).to be_nil
    expect(@result[:errors].first[:message])
      .to eq "User ID not authorized!"
  end

  context "searches properly" do
    before :each do
      @model = FactoryBot.create(:leihs_model,
        product: "Kabelrolle 230V",
        version: "50m",
        manufacturer: "Steffen")
      FactoryBot.create(:item,
        leihs_model: @model,
        responsible: @inventory_pool,
        is_borrowable: true)
    end

    it "match" do
      q = <<-GRAPHQL
        query($searchTerm: String) {
          models(searchTerm: $searchTerm) {
            edges {
              node {
                id
              }
            }
          }
        }
      GRAPHQL

      search_terms = ["kabel",
        "rolle",
        "kabelrolle 50m 230v",
        "230v 50m",
        "steffen",
        "steffen 230v"]

      search_terms.each do |t|
        vars = {searchTerm: t}
        result = query(q, @user.id, vars).deep_symbolize_keys
        model_ids = result.dig(:data, :models, :edges).map { |n| n[:node][:id] }
        expect(model_ids.count).to eq 1
        expect(model_ids.first).to eq @model.id
      end
    end

    context "no match because of reservation_advance_days" do
      include_context "reservation advance days"

      it "works" do
        q = ->(date) {
          <<-GRAPHQL
            {
              models(onlyAvailable: true) {
                edges {
                  node {
                    id
                    availableQuantityInDateRange(
                      startDate: "#{date}",
                      endDate: "#{date}"
                    )
                  }
                }
              }
            }
          GRAPHQL
        }

        model_ids = ->(result) {
          result.dig(:data, :models, :edges).map { |n| n[:node][:id] }
        }

        result = query(q.call(Date.today + 4.days), @user.id).deep_symbolize_keys
        expect(model_ids.call(result)).to be_empty

        result = query(q.call(Date.today + 5.days), @user.id).deep_symbolize_keys
        expect(model_ids.call(result)).not_to be_empty
      end
    end
  end
end
