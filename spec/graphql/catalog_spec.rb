require 'spec_helper'
require_relative 'graphql_helper'

def factorize!(arg)
  case arg
  when Array
    arg.map { |x| factorize!(x) }
  when Hash
    factory = arg.delete(:factory)
    attrs = arg.map { |k, v| [k, factorize!(v)] }.to_h
    FactoryBot.create(factory, attrs)
  else
    arg
  end
end

describe 'catalog' do
  it 'works' do
    data = [
      { factory: :category,
        id: '9a1dc177-a2b2-4a16-8fbf-6552b5313f38',
        direct_models: [
          { factory: :model,
            id: '48e7de51-a1d3-4651-9afa-c5a185594e50' }
        ],
        children: [
          { factory: :category,
            id: '33df18c8-6d86-44a1-a0d8-d76847d8b043',
            direct_models: [
              { factory: :model,
                id: 'f39b95d2-fcef-4b66-96ec-b86de1d7238b' }
            ],
            children: [
              { factory: :category,
                id: 'ef364d34-9ed5-4b51-bdff-17885e48c8bc',
                direct_models: [
                  { factory: :model,
                    id: '0d082f18-e42b-4097-a73f-a1e970d86246' }
                ]
              }
            ]
          }
        ]
      }
    ]

    factorize!(data)

    q = <<-GRAPHQL
      query {
        categories(rootOnly: true) {
          id
          directModels: models(
            directOnly: true,
            order: [{attribute: ID, direction: ASC}]
          ) {
            id
          }
          models {
            id
          }
          children {
            id
            children {
              id
              children {
                id
              }
            }
          }
        }
      }
    GRAPHQL

    result = query(q)

    expect(result).to include({
      'data' => {
        'categories' => [
          { 'id' => '9a1dc177-a2b2-4a16-8fbf-6552b5313f38',
            'directModels' => [
              { 'id' => '48e7de51-a1d3-4651-9afa-c5a185594e50' }
            ],
            'models' => [
              { 'id' => '0d082f18-e42b-4097-a73f-a1e970d86246' },
              { 'id' => '48e7de51-a1d3-4651-9afa-c5a185594e50' },
              { 'id' => 'f39b95d2-fcef-4b66-96ec-b86de1d7238b' }
            ],
            'children' => [
              { 'id' => '33df18c8-6d86-44a1-a0d8-d76847d8b043',
                'children' => [
                  { 'id' => 'ef364d34-9ed5-4b51-bdff-17885e48c8bc',
                    'children' => [] }
                ]
              }
            ]
          }
        ]
      }
    })

    expect(result).not_to include(:errors)
  end
end
