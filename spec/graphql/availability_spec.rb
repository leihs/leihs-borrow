require 'spec_helper'
require_relative 'graphql_helper'

describe 'availability' do
  it 'works' do
    user = FactoryBot.create(
      :user,
      id: '50a33303-6b86-4908-8e8e-f7a3c606ba7c'
    )

    q = <<-GRAPHQL
      query {
        availability(
          modelId: "91f2c252-ebb4-4265-8806-4669c8626913",
          inventoryPoolId: "aae826c3-dcb6-407e-a72b-1853bf1911e7",
          startDate: "2019-10-24",
          endDate: "2019-10-25") {
          dates {
            date
            quantity
          }
        }
      }
    GRAPHQL

    result = query(q, user.id).deep_symbolize_keys
    expect(result[:data]).to eq({
      availability: {
        dates: [
          { date: "2019-10-24",
            quantity: 1 }
        ]
      }
    })
    expect(result[:errors]).to be_nil
  end
end
