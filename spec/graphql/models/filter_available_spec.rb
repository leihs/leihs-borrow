require "spec_helper"
require_relative "../graphql_helper"

describe "filter available models" do
  before :each do
    @user = FactoryBot.create(
      :user,
      id: "d15f44e2-244b-416c-8ed6-d8f79da3b7bc"
    )
    @user2 = FactoryBot.create(
      :user,
      id: "4f6af66e-b172-4c6e-b338-9db9af7911bb"
    )
    @inventory_pool = FactoryBot.create(
      :inventory_pool,
      id: "f335cd67-9b1e-48ee-a2f9-e265b91dc58d"
    )
    FactoryBot.create(:direct_access_right,
      inventory_pool: @inventory_pool,
      user: @user)
    FactoryBot.create(:direct_access_right,
      inventory_pool: @inventory_pool,
      user: @user2)
  end

  let(:q) do
    <<-GRAPHQL
      {
        models(
          orderBy: [{attribute: NAME, direction: ASC}],
          onlyAvailable: true,
          first: 2
        ) {
          edges {
            node {
              id
              availableQuantityInDateRange(
                startDate: "#{Date.today}",
                endDate: "#{Date.tomorrow}"
              )
            }
          }
          pageInfo {
            hasNextPage
          }
        }
      }
    GRAPHQL
  end

  it "first on page 1, last on page 3" do
    ############### PAGE 1 ###############
    FactoryBot.create(
      :leihs_model,
      id: "e5509e8e-95fc-4772-800c-dcdd8402789a",
      product: "Model A"
    )
    m2 = FactoryBot.create(
      :leihs_model,
      id: "4d15f802-47ad-4862-b595-1df259c118b6",
      product: "Model B"
    )
    FactoryBot.create(:reservation,
      leihs_model: m2,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    ############### PAGE 2 ###############
    m3 = FactoryBot.create(
      :leihs_model,
      id: "e63ea4dd-cd08-494e-864f-3092e63582e5",
      product: "Model C"
    )
    FactoryBot.create(:reservation,
      leihs_model: m3,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")

    m4 = FactoryBot.create(
      :leihs_model,
      id: "b4bfcdb5-5e5e-47cd-a8a8-8ab0bb4222c1",
      product: "Model D"
    )
    FactoryBot.create(:reservation,
      leihs_model: m4,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    ############### PAGE 3 ###############
    FactoryBot.create(
      :leihs_model,
      id: "2821679a-90b0-498d-928e-82137b1ec3ed",
      product: "Model E"
    )

    LeihsModel.all.map do |model|
      FactoryBot.create(:item,
        leihs_model: model,
        responsible: @inventory_pool,
        is_borrowable: true)
    end

    result = query(q, @user.id)
    expect_graphql_result(result, {
      models: {
        edges: [
          {node: {id: "e5509e8e-95fc-4772-800c-dcdd8402789a",
                  availableQuantityInDateRange: 1}},
          {node: {id: "2821679a-90b0-498d-928e-82137b1ec3ed",
                  availableQuantityInDateRange: 1}}
        ],
        pageInfo: {
          hasNextPage: false
        }
      }
    })
  end

  it "first 2 on page 1, last on page 3" do
    ############### PAGE 1 ###############
    FactoryBot.create(
      :leihs_model,
      id: "e5fa8a1b-aeb6-4a9a-9b7c-9437f2edd244",
      product: "Model A"
    )
    FactoryBot.create(
      :leihs_model,
      id: "915fd455-1757-4888-a117-9719e64644c4",
      product: "Model B"
    )
    ############### PAGE 2 ###############
    m3 = FactoryBot.create(
      :leihs_model,
      id: "317d9660-5eb1-4c38-b763-eca6e35ee20f",
      product: "Model C"
    )
    FactoryBot.create(:reservation,
      leihs_model: m3,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    m4 = FactoryBot.create(
      :leihs_model,
      id: "eddd32a1-d465-430f-91ac-c26530cb0e13",
      product: "Model D"
    )
    FactoryBot.create(:reservation,
      leihs_model: m4,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    ############### PAGE 3 ###############
    FactoryBot.create(
      :leihs_model,
      id: "5fb44c5e-0027-4862-8d77-67422564d414",
      product: "Model E"
    )

    LeihsModel.all.map do |model|
      FactoryBot.create(:item,
        leihs_model: model,
        responsible: @inventory_pool,
        is_borrowable: true)
    end

    result = query(q, @user.id)
    expect_graphql_result(result, {
      models: {
        edges: [
          {node: {id: "e5fa8a1b-aeb6-4a9a-9b7c-9437f2edd244",
                  availableQuantityInDateRange: 1}},
          {node: {id: "915fd455-1757-4888-a117-9719e64644c4",
                  availableQuantityInDateRange: 1}}
        ],
        pageInfo: {
          hasNextPage: true
        }
      }
    })
  end

  it "first 2 on page 1" do
    ############### PAGE 1 ###############
    FactoryBot.create(
      :leihs_model,
      id: "ee367c0a-26ee-47aa-bdf7-55a4b7a36841",
      product: "Model A"
    )
    FactoryBot.create(
      :leihs_model,
      id: "b7dc6e69-9ab8-4e2b-87cb-30c16ec1dd6c",
      product: "Model B"
    )
    ############### PAGE 2 ###############
    m3 = FactoryBot.create(
      :leihs_model,
      id: "d6536c1a-4977-4710-9055-34530e6edbfe",
      product: "Model C"
    )
    FactoryBot.create(:reservation,
      leihs_model: m3,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    m4 = FactoryBot.create(
      :leihs_model,
      id: "cf032455-8443-4aa2-bd41-0ab374720b73",
      product: "Model D"
    )
    FactoryBot.create(:reservation,
      leihs_model: m4,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    ############### PAGE 3 ###############
    m5 = FactoryBot.create(
      :leihs_model,
      id: "2d70b519-1fa6-4213-b360-6ecffaf375fc",
      product: "Model E"
    )
    FactoryBot.create(:reservation,
      leihs_model: m5,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")

    LeihsModel.all.map do |model|
      FactoryBot.create(:item,
        leihs_model: model,
        responsible: @inventory_pool,
        is_borrowable: true)
    end

    result = query(q, @user.id)
    expect_graphql_result(result, {
      models: {
        edges: [
          {node: {id: "ee367c0a-26ee-47aa-bdf7-55a4b7a36841",
                  availableQuantityInDateRange: 1}},
          {node: {id: "b7dc6e69-9ab8-4e2b-87cb-30c16ec1dd6c",
                  availableQuantityInDateRange: 1}}
        ],
        pageInfo: {
          hasNextPage: false
        }
      }
    })
  end

  it "one on page 1" do
    ############### PAGE 1 ###############
    FactoryBot.create(
      :leihs_model,
      id: "3eafc404-a1c0-4482-b185-f3d73135cffa",
      product: "Model A"
    )
    m2 = FactoryBot.create(
      :leihs_model,
      id: "649bebbe-144a-4672-8f7f-131d1868d36b",
      product: "Model B"
    )
    FactoryBot.create(:reservation,
      leihs_model: m2,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    ############### PAGE 2 ###############
    m3 = FactoryBot.create(
      :leihs_model,
      id: "bea60deb-3074-43fb-a95b-55e404ac056c",
      product: "Model C"
    )
    FactoryBot.create(:reservation,
      leihs_model: m3,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    m4 = FactoryBot.create(
      :leihs_model,
      id: "83b7eca5-1ad0-418b-b018-6a27046dc388",
      product: "Model D"
    )
    FactoryBot.create(:reservation,
      leihs_model: m4,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    ############### PAGE 3 ###############
    m5 = FactoryBot.create(
      :leihs_model,
      id: "65e85b5b-4b18-4a8f-8c4b-fff2e9cd374f",
      product: "Model E"
    )
    FactoryBot.create(:reservation,
      leihs_model: m5,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")

    LeihsModel.all.map do |model|
      FactoryBot.create(:item,
        leihs_model: model,
        responsible: @inventory_pool,
        is_borrowable: true)
    end

    result = query(q, @user.id)
    expect_graphql_result(result, {
      models: {
        edges: [
          {node: {id: "3eafc404-a1c0-4482-b185-f3d73135cffa",
                  availableQuantityInDateRange: 1}}
        ],
        pageInfo: {
          hasNextPage: false
        }
      }
    })
  end

  it "one on page 2" do
    ############### PAGE 1 ###############
    m1 = FactoryBot.create(
      :leihs_model,
      id: "6ab9d55e-b806-40d9-ad10-8fb94361568d",
      product: "Model A"
    )
    FactoryBot.create(:reservation,
      leihs_model: m1,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    m2 = FactoryBot.create(
      :leihs_model,
      id: "fb502498-7391-4bdd-9353-11daa55b1cc3",
      product: "Model B"
    )
    FactoryBot.create(:reservation,
      leihs_model: m2,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    ############### PAGE 2 ###############
    FactoryBot.create(
      :leihs_model,
      id: "a61af742-f807-42f7-a917-f390eebc139d",
      product: "Model C"
    )
    m4 = FactoryBot.create(
      :leihs_model,
      id: "11327f34-ccf9-405d-b099-5abfd9d73596",
      product: "Model D"
    )
    FactoryBot.create(:reservation,
      leihs_model: m4,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    ############### PAGE 3 ###############
    m5 = FactoryBot.create(
      :leihs_model,
      id: "b14656a7-019f-40d7-b27d-c46be0c88df6",
      product: "Model E"
    )
    FactoryBot.create(:reservation,
      leihs_model: m5,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")

    LeihsModel.all.map do |model|
      FactoryBot.create(:item,
        leihs_model: model,
        responsible: @inventory_pool,
        is_borrowable: true)
    end

    result = query(q, @user.id)
    expect_graphql_result(result, {
      models: {
        edges: [
          {node: {id: "a61af742-f807-42f7-a917-f390eebc139d",
                  availableQuantityInDateRange: 1}}
        ],
        pageInfo: {
          hasNextPage: false
        }
      }
    })
  end

  it "one on page 3" do
    ############### PAGE 1 ###############
    m1 = FactoryBot.create(
      :leihs_model,
      id: "589435ab-64e6-497e-a177-2b7a3a00aef7",
      product: "Model A"
    )
    FactoryBot.create(:reservation,
      leihs_model: m1,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    m2 = FactoryBot.create(
      :leihs_model,
      id: "5373b0d3-4b7c-4223-9e48-98f161e6d6d8",
      product: "Model B"
    )
    FactoryBot.create(:reservation,
      leihs_model: m2,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    ############### PAGE 2 ###############
    m3 = FactoryBot.create(
      :leihs_model,
      id: "0212710d-e152-4020-8b95-e53cf5c748e9",
      product: "Model C"
    )
    FactoryBot.create(:reservation,
      leihs_model: m3,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    m4 = FactoryBot.create(
      :leihs_model,
      id: "a402c1e4-5c7e-4108-bcb7-e0cae3602612",
      product: "Model D"
    )
    FactoryBot.create(:reservation,
      leihs_model: m4,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")
    ############### PAGE 3 ###############
    FactoryBot.create(
      :leihs_model,
      id: "db3f1cb0-aa2d-4148-b9f5-adb2a0d208af",
      product: "Model E"
    )

    LeihsModel.all.map do |model|
      FactoryBot.create(:item,
        leihs_model: model,
        responsible: @inventory_pool,
        is_borrowable: true)
    end

    result = query(q, @user.id)
    expect_graphql_result(result, {
      models: {
        edges: [
          {node: {id: "db3f1cb0-aa2d-4148-b9f5-adb2a0d208af",
                  availableQuantityInDateRange: 1}}
        ],
        pageInfo: {
          hasNextPage: false
        }
      }
    })
  end
end
