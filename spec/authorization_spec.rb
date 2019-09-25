# require 'uuidtools'
# require 'spec_helper'
# require_relative 'graphql/graphql_helper'

# describe 'authorization' do
#   it 'returns 401 if not authenticated' do
#     q = <<-GRAPHQL
#       query {
#         admins { 
#           id
#         }
#       }
#     GRAPHQL

#     user_id = UUIDTools::UUID.random_create.to_s
#     response = GraphqlQuery.new(q, user_id).perform.response
#     expect(response.status).to eq(401)
#   end

#   it 'returns 403 if not procurement access' do
#     q = <<-GRAPHQL
#       query {
#         admins { 
#           id
#         }
#       }
#     GRAPHQL

#     user = FactoryBot.create(:user)
#     response = GraphqlQuery.new(q, user.id).perform.response
#     expect(response.status).to eq(403)
#   end
# end
