require 'spec_helper'
require_relative 'graphql_helper'

describe 'catalog' do
  it 'works' do
    category_I = FactoryBot.create(:category, name: "category_I")
    category_I_1 = FactoryBot.create(:category,
                                     name: "category_I_1",
                                     parents: [category_I])
    model_A_from_I_1 = FactoryBot.create(:model,
                                         categories: [category_I_1])


    # q = <<-GRAPHQL
    #   query {
    #     users {
    #       id
    #     }
    #   }
    # GRAPHQL

    # result = query(q)

    # expect(result).to include({
    #   'data' => {
    #     'users' => [
    #       { 'id' => user1.id },
    #       { 'id' => user2.id }
    #     ]
    #   }
    # })

    # expect(result).not_to include(:errors)
  end
end

