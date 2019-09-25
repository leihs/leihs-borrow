require 'spec_helper'
require_relative 'graphql_helper'

describe 'users' do
  context 'filter' do
    it 'requesters only' do
      user1 = FactoryBot.create(:user)
      user2 = FactoryBot.create(:user)
      FactoryBot.create(:requester_organization,
                        user_id: user2.id)

      admin = FactoryBot.create(:user)
      FactoryBot.create(:admin, user_id: admin.id)

      q = <<-GRAPHQL
        query {
          users(isRequester: true) {
            id
          }
        }
      GRAPHQL

      result = query(q, admin.id)
      expect(result).to eq({
        'data' => {
          'users' => [
            { 'id' => user2.id }
          ]
        }
      })
    end
  end
end
