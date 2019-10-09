require 'spec_helper'
require_relative 'graphql_helper'

describe 'models' do
  it 'works' do
    model = FactoryBot.create(
      :leihs_model,
      id: '2bc1deb5-9428-4178-afd0-c06bb8d31ff3',
      properties: [
        FactoryBot.create(
          :property,
          id: '2df736a4-825c-4f36-b48a-75875b3a3c26'
        )
      ],
      images: [
        FactoryBot.create(
          :image,
          :for_leihs_model,
          id: '7484b5d2-376a-4b15-8db0-54cc6bab02ea'
        )
      ]
    )

    model.add_attachment(
      FactoryBot.build(
        :attachment,
        id: '919fbdd1-111c-49b7-aeb0-2d5d8825ed00'
      )
    )

    q = <<-GRAPHQL
      {
        models {
          id
          images {
            imageUrl
          }
          attachments {
            url
          }
        }
      }
    GRAPHQL

    result = query(q)
    
    expect(result['data']).to eq({
      'models' => [
        { 'id' => '2bc1deb5-9428-4178-afd0-c06bb8d31ff3',
          'images' => [
            { 'imageUrl' => '/borrow/images/7484b5d2-376a-4b15-8db0-54cc6bab02ea' }
          ],
          'attachments' => [
            { 'url' => '/borrow/attachments/919fbdd1-111c-49b7-aeb0-2d5d8825ed00' }
          ]
        }
      ]
    })

    expect(result).not_to include(:errors)
  end
end
