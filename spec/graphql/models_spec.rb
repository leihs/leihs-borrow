require 'spec_helper'
require_relative 'graphql_helper'

describe 'models' do
  it 'works' do
    data = [
      { factory: :model,
        id: '2bc1deb5-9428-4178-afd0-c06bb8d31ff3' },
      { factory: :image,
        trait: :for_model,
        id: '7484b5d2-376a-4b15-8db0-54cc6bab02ea',
        target_id: '2bc1deb5-9428-4178-afd0-c06bb8d31ff3' },
      { factory: :attachment,
        id: '919fbdd1-111c-49b7-aeb0-2d5d8825ed00',
        model_id: '2bc1deb5-9428-4178-afd0-c06bb8d31ff3' }
    ]

    factorize!(data)

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
