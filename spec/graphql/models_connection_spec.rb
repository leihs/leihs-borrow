require 'spec_helper'
require_relative 'graphql_helper'

def cursor(uuid)
  database[
    "SELECT encode(decode(replace('#{uuid}', '-', ''), 'hex'), 'base64') AS result"
  ].first[:result]
end

class Hash
  def delete_in!(*path, key)
    self.dig(*path).try(:delete, key)
  end
end

describe 'models connection' do
  after :each do
    # expect(@result).not_to include(:errors)
  end

  before :each do
    @user = FactoryBot.create(
      :user,
      id: 'ce3665a0-2711-44b8-aa47-11fb881c3f91'
    )
    @inventory_pool = FactoryBot.create(
      :inventory_pool,
      id: '8f613f14-3b6d-4d5c-9804-913e2da1109e'
    )
    FactoryBot.create(:access_right,
                      inventory_pool: @inventory_pool,
                      user: @user)

    @model_1 = FactoryBot.create(
      :leihs_model,
      id: '0cad263d-14b9-4595-9878-7adde7f4f586'
    )
    @model_2 = FactoryBot.create(
      :leihs_model,
      id: '2adfe883-3546-4b5c-9ed6-b18f01f77723'
    )
    @model_3 = FactoryBot.create(
      :leihs_model,
      id: '89c1bdf9-7764-4e1e-bf9e-902f908be8d5'
    )

    LeihsModel.all.map do |model|
      FactoryBot.create(:item,
                        leihs_model: model,
                        responsible: @inventory_pool,
                        is_borrowable: true)
    end
  end

  it 'get each successively' do
    # get all without `first` and `after`
    q = <<-GRAPHQL
      {
        modelsConnection(orderBy: [{attribute: ID, direction: ASC}]) {
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
      :modelsConnection => {
        :totalCount => 3,
        :edges => [
          { :node => {
            :id => @model_1.id
            }
          },
          { :node => {
            :id => @model_2.id
            }
          },
          { :node => {
            :id => @model_3.id
            }
          }
        ],
        :pageInfo => {
          :hasNextPage => false
        }
      }
    })

    # get first one without `after`
    q = <<-GRAPHQL
      {
        modelsConnection(
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
    ec = @result.delete_in!(:data, :modelsConnection, :pageInfo, :endCursor)

    expect(@result[:data]).to eq({
      :modelsConnection => {
        :totalCount => 3,
        :edges => [
          { :node => {
            :id => @model_1.id },
          }
        ],
        :pageInfo => {
          :hasNextPage => true
        }
      }
    })

    # get second one with `after` and `first`
    q = <<-GRAPHQL
      {
        modelsConnection(
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
    ec = @result.delete_in!(:data, :modelsConnection, :pageInfo, :endCursor)

    expect(@result[:data]).to eq({
      :modelsConnection => {
        :totalCount => 3,
        :edges => [
          { :node => {
            :id => @model_2.id
            }
          }
        ],
        :pageInfo => {
          :hasNextPage => true
        }
      }
    })

    # get third one with `after` and `first`
    q = <<-GRAPHQL
      {
        modelsConnection(
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
    ec = @result.delete_in!(:data, :modelsConnection, :pageInfo, :endCursor)

    expect(@result[:data]).to eq({
      :modelsConnection => {
        :totalCount => 3,
        :edges => [
          { :node => {
            :id => @model_3.id
            }
          }
        ],
        :pageInfo => {
          :hasNextPage => false
        }
      }
    })

    # go beyond what's available
    q = <<-GRAPHQL
      {
        modelsConnection(
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

    expect(@result[:data]).to eq({
      :modelsConnection => {
        :totalCount => 3,
        :edges => [],
        :pageInfo => {
          :endCursor => nil,
          :hasNextPage => false
        }
      }
    })
  end

  it 'get 1st 2' do
    q = <<-GRAPHQL
      {
        modelsConnection(first: 2, orderBy: [{attribute: ID, direction: ASC}]) {
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
      :modelsConnection => {
        :totalCount => 3,
        :edges => [
          { :node => {
            :id => @model_1.id },
          },
          { :node => {
            :id => @model_2.id },
          }
        ],
        :pageInfo => {
          :hasNextPage => true
        }
      }
    })
  end

  it 'get all after 1st' do
    q = <<-GRAPHQL
      {
        modelsConnection(
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

    expect(@result[:data]).to eq({
      :modelsConnection => {
        :totalCount => 3,
        :edges => [
          { :node => {
            :id => @model_2.id
            }
          },
          { :node => {
            :id => @model_3.id
            }
          }
        ],
        :pageInfo => {
          :hasNextPage => false
        }
      }
    })
  end
end
