require "spec_helper"
require_relative "graphql_helper"

describe "favorites" do
  before :each do
    @user = FactoryBot.create(
      :user,
      id: "fdf1db47-67db-4341-9e66-9fc18988149a"
    )
    @model = FactoryBot.create(
      :leihs_model,
      id: "57f7fe71-46d2-4aa3-ae12-c6fc09c7b7a3"
    )
  end

  context "create" do
    let(:q) do
      <<-GRAPHQL
        mutation {
          favoriteModel(id: "#{@model.id}") {
            id
          }
        }
      GRAPHQL
    end

    example "works" do
      result = query(q, @user.id)
      expect_graphql_result(result, {
        favoriteModel: {
          id: @model.id
        }
      })
    end

    example "idempotence" do
      result_1 = query(q, @user.id)
      expect_graphql_result(result_1, {
        favoriteModel: {
          id: @model.id
        }
      })
      result_2 = query(q, @user.id)
      expect_graphql_result(result_2, result_1[:data])
    end
  end

  context "delete" do
    let(:q) do
      <<-GRAPHQL
        mutation {
          unfavoriteModel(id: "#{@model.id}") {
            id
          }
        }
      GRAPHQL
    end

    example "works" do
      FactoryBot.create(:favorite_model,
        user: @user,
        leihs_model: @model)

      result = query(q, @user.id)
      expect_graphql_result(result, {
        unfavoriteModel: {
          id: @model.id
        }
      })
    end

    example "idempotence" do
      result = query(q, @user.id)
      expect_graphql_result(result, {
        unfavoriteModel: {
          id: @model.id
        }
      })
    end
  end
end
