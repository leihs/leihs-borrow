require 'spec_helper'
require_relative 'graphql_helper'

describe 'users' do
  it 'works' do
    user1 = FactoryBot.create(:user)
    user2 = FactoryBot.create(:user)

    q = <<-GRAPHQL
        query {
          users {
            id
          }
        }
    GRAPHQL

    result = query(q)

    expect(result).to include({
      'data' => {
        'users' => [
          { 'id' => user1.id },
          { 'id' => user2.id }
        ]
      }
    })

    expect(result).not_to include(:errors)
  end
end
