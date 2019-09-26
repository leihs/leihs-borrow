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
        children: [
          { factory: :category,
            id: '33df18c8-6d86-44a1-a0d8-d76847d8b043',
            direct_models: [
              { factory: :model,
                id: 'f39b95d2-fcef-4b66-96ec-b86de1d7238b',
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
        }
      }
    GRAPHQL

    result = query(q)

    expect(result).to include({
      'data' => {
        'categories' => [
          { 'id' => '9a1dc177-a2b2-4a16-8fbf-6552b5313f38' }
        ]
      }
    })

    expect(result).not_to include(:errors)
  end
end
