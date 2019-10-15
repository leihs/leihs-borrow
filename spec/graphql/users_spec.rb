require 'spec_helper'
require_relative 'graphql_helper'

describe 'users' do
  it 'works' do
    user1 = FactoryBot.create(:user,
                              id: 'ab1e73cb-97a0-49d8-872b-1067832a5b86')
    user2 = FactoryBot.create(:user,
                              id: 'd8b2e006-e8c0-43ec-b9df-54108e0e0dd0')

    q = <<-GRAPHQL
      query {
        users(orderBy: [{attribute: ID, direction: ASC}]) {
          id
        }
      }
    GRAPHQL

    result = query(q, user1.id)

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
