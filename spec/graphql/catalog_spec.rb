require 'spec_helper'
require_relative 'graphql_helper'

def factorize!(arg)
  case arg
  when Array
    arg.map { |x| factorize!(x) }
  when Hash
    factory = arg.delete(:factory)
    raise 'No :factory key given!' unless factory
    attrs = arg.map { |k, v| [k, factorize!(v)] }.to_h
    # binding.pry if factory == :item
    FactoryBot.create(factory, attrs)
  else
    arg
  end
end

describe 'catalog' do
  it 'works' do
    data = [
      { factory: :inventory_pool,
        id: '93c17c42-50d6-4af9-aa3b-96a0aafb8011'
      },
      { factory: :user,
        id: '3867c467-6dfc-4fb2-83ed-993aa774d762',
        access_rights: [
          { factory: :access_right,
            inventory_pool_id: '93c17c42-50d6-4af9-aa3b-96a0aafb8011' }
        ]
      },
      { factory: :category,
        id: '9a1dc177-a2b2-4a16-8fbf-6552b5313f38',
        direct_models: [
          { factory: :model },
          { factory: :model,
            id: '48e7de51-a1d3-4651-9afa-c5a185594e50',
            items: [
              { factory: :item,
                inventory_pool_id: '93c17c42-50d6-4af9-aa3b-96a0aafb8011',
                is_borrowable: true }
            ]
          }
        ],
        children: [
          { factory: :category,
            id: '33df18c8-6d86-44a1-a0d8-d76847d8b043',
            direct_models: [
              { factory: :model },
              { factory: :model,
                id: 'f39b95d2-fcef-4b66-96ec-b86de1d7238b',
                items: [
                  { factory: :item,
                    inventory_pool_id: '93c17c42-50d6-4af9-aa3b-96a0aafb8011',
                    is_borrowable: true }
                ]
              }
            ],
            children: [
              { factory: :category,
                id: 'ef364d34-9ed5-4b51-bdff-17885e48c8bc',
                direct_models: [
                  { factory: :model },
                  { factory: :model,
                    id: '0d082f18-e42b-4097-a73f-a1e970d86246',
                    items: [
                      { factory: :item,
                        inventory_pool_id: '93c17c42-50d6-4af9-aa3b-96a0aafb8011',
                        is_borrowable: true }
                    ]
                  }
                ]
              }
            ]
          },
          { factory: :category,
            direct_models: [
              { factory: :model,
                items: [
                  { factory: :item,
                    is_borrowable: true }
                ]
              }
            ]
          }
        ]
      },
      { factory: :category,
        direct_models: [
          { factory: :model,
            items: [
              { factory: :item,
                is_borrowable: true }
            ]
          }
        ]
      }
    ]

    factorize!(data)

    q = <<-GRAPHQL
      query Catalog($idAsc: [ModelsOrderByInput]!, $userId: UUID!) {
        categories(rootOnly: true, userId: $userId) {
          id
          ...directModelsField
          ...modelsField
          children(userId: $userId) {
            id
            ...directModelsField
            ...modelsField
            children(userId: $userId) {
              id
              ...directModelsField
              ...modelsField
              children(userId: $userId) {
                id
                ...directModelsField
                ...modelsField
              }
            }
          }
        }
      }

      fragment directModelsField on Category {
        directModels: models(directOnly: true, orderBy: $idAsc, userId: $userId) {
          id
        }
      }

      fragment modelsField on Category {
        models(userId: $userId) {
          id
        }
      }
    GRAPHQL

    vars = {
      idAsc: [{attribute: 'ID', direction: 'ASC'}],
      userId: '3867c467-6dfc-4fb2-83ed-993aa774d762' 
    }

    result = query(q, nil, vars)
    
    expect(result['data']).to eq({
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
              'directModels' => [
                { 'id' => 'f39b95d2-fcef-4b66-96ec-b86de1d7238b' }
              ],
              'models' => [
                { 'id' => '0d082f18-e42b-4097-a73f-a1e970d86246' },
                { 'id' => 'f39b95d2-fcef-4b66-96ec-b86de1d7238b' }
              ],
              'children' => [
                { 'id' => 'ef364d34-9ed5-4b51-bdff-17885e48c8bc',
                  'directModels' => [
                    { 'id' => '0d082f18-e42b-4097-a73f-a1e970d86246' }
                  ],
                  'models' => [
                    { 'id' => '0d082f18-e42b-4097-a73f-a1e970d86246' }
                  ],
                  'children' => [] }
              ]
            }
          ]
        }
      ]
    })

    expect(result).not_to include(:errors)
  end
end
