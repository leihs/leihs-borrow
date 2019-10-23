require 'spec_helper'
require_relative 'graphql_helper'

describe 'schema' do
  it 'bypasses authentication' do
    q = <<-GRAPHQL
      query {
        __schema {
          queryType {
            name
          }
        }
      }
    GRAPHQL

    result = query(q).deep_symbolize_keys

    expect(result.dig(:data, :__schema, :queryType, :name))
      .to eq 'QueryRoot'
    expect(result).not_to include(:errors)
  end
end

