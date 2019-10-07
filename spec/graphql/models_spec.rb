require 'spec_helper'
require_relative 'graphql_helper'

describe 'models' do
  it 'works' do
    data = [
      { factory: :model,
        id: '2bc1deb5-9428-4178-afd0-c06bb8d31ff3',
        images: [
          { factory: :image,
            trait: :for_model,
            id: '041c45ce-fbd7-4b99-8f72-3d48a4db96a2',
          }
        ]
      }
    ]

    factorize!(data)

    q = <<-GRAPHQL
      {
        models {
          id
          images {
            imageUrl
          }
        }
      }
    GRAPHQL

    result = query(q)
    
    expect(result['data']).to eq({
      'models' => [
        { 'id' => '2bc1deb5-9428-4178-afd0-c06bb8d31ff3',
          'images' => [
            { 'imageUrl' => '/borrow/images/041c45ce-fbd7-4b99-8f72-3d48a4db96a2' }
          ]
        }
      ]
    })

    expect(result).not_to include(:errors)
  end
end
