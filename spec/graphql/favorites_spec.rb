require 'spec_helper'
require_relative 'graphql_helper'

describe 'favorites' do
  before :each do
    @user = FactoryBot.create(
      :user,
      id: 'fdf1db47-67db-4341-9e66-9fc18988149a'
    )
    @model = FactoryBot.create(
      :leihs_model,
      id: '57f7fe71-46d2-4aa3-ae12-c6fc09c7b7a3'
    )
  end

  context 'create' do
    let(:q) do
      <<-GRAPHQL
        mutation {
          favoriteModel(id: "#{@model.id}") {
            id
          }
        }
      GRAPHQL
    end

    example 'works' do
      result = query(q, @user.id)
      expect_graphql_result(result, {
        :favoriteModel => {
          :id => @model.id
        }
      })
    end

    example 'unique constraint' do
      result_1 = query(q, @user.id)
      expect_graphql_result(result_1, {
        :favoriteModel => {
          :id => @model.id
        }
      })
      result_2 = query(q, @user.id)
      expect_graphql_error(result_2)
      expect(@user.favorite_models.count).to eq 1
    end
  end

  example 'delete' do
    FactoryBot.create(:favorite_model,
                      user: @user,
                      leihs_model: @model)

    q = \
      <<-GRAPHQL
        mutation {
          unfavoriteModel(id: "#{@model.id}") {
            id
          }
        }
      GRAPHQL

    result = query(q, @user.id)
    expect_graphql_result(result, {
      :unfavoriteModel => {
        :id => @model.id
      }
    })
  end
end
