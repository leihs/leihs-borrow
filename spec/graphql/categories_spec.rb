require 'spec_helper'
require_relative 'graphql_helper'

describe 'categories' do
  it 'works' do
    inventory_pool = FactoryBot.create(:inventory_pool,
                                       id: '93c17c42-50d6-4af9-aa3b-96a0aafb8011')
    user = FactoryBot.create(:user,
                             id: '3867c467-6dfc-4fb2-83ed-993aa774d762')
    FactoryBot.create(:access_right,
                      inventory_pool: inventory_pool,
                      user: user)

    FactoryBot.create(
      :category,
      id: '9a1dc177-a2b2-4a16-8fbf-6552b5313f38',
      images: [
        FactoryBot.create(
          :image, :for_category,
          id: '081c8aa7-b514-4935-a9ea-bd698f378d9a',
          thumbnails: [
            FactoryBot.create(:image, :for_category,
                              id: '82085a7b-b428-4c4b-b977-efbc2045ff46')
          ]
        )
      ],
      direct_models: [
        FactoryBot.create(:leihs_model),
        FactoryBot.create(
          :leihs_model,
          id: '48e7de51-a1d3-4651-9afa-c5a185594e50',
          items: [
            FactoryBot.create(
              :item,
              responsible: inventory_pool,
              is_borrowable: true
            )
          ]
        )
      ],
      children: [
        FactoryBot.create(
          :category,
          id: '33df18c8-6d86-44a1-a0d8-d76847d8b043',
          direct_models: [
            FactoryBot.create(:leihs_model),
            FactoryBot.create(
              :leihs_model,
              id: 'f39b95d2-fcef-4b66-96ec-b86de1d7238b',
              items: [
                FactoryBot.create(
                  :item,
                  responsible: inventory_pool,
                  is_borrowable: true
                )
              ]
            )
          ],
          children: [
            FactoryBot.create(
              :category,
              id: 'ef364d34-9ed5-4b51-bdff-17885e48c8bc',
              direct_models: [
                FactoryBot.create(:leihs_model),
                FactoryBot.create(
                  :leihs_model,
                  id: '0d082f18-e42b-4097-a73f-a1e970d86246',
                  items: [
                    FactoryBot.create(
                      :item,
                      responsible: inventory_pool,
                      is_borrowable: true
                    )
                  ]
                )
              ]
            )
          ]
        )
      ]
    )

    FactoryBot.create(
      :category,
      direct_models: [
        FactoryBot.create(
          :leihs_model,
          items: [
            FactoryBot.create(
              :item,
              is_borrowable: true
            )
          ]
        )
      ]
    )

    q = <<-GRAPHQL
      query Catalog($idAsc: [ModelsOrderByInput]!) {
        categories(rootOnly: true) {
          id
          ...directModelsField
          ...modelsField
          images {
            imageUrl
            thumbnails {
              imageUrl
            }
          }
          children {
            id
            ...directModelsField
            ...modelsField
            children {
              id
              ...directModelsField
              ...modelsField
              children {
                id
                ...directModelsField
                ...modelsField
              }
            }
          }
        }
      }

      fragment directModelsField on Category {
        directModels: models(directOnly: true, orderBy: $idAsc) {
          id
        }
      }

      fragment modelsField on Category {
        models {
          id
        }
      }
    GRAPHQL

    vars = {
      idAsc: [{attribute: 'ID', direction: 'ASC'}]
    }

    result = query(q, user.id, vars)
    
    expect(result['data']).to eq({
      'categories' => [
        { 'id' => '9a1dc177-a2b2-4a16-8fbf-6552b5313f38',
          'images' => [
            { 'imageUrl' => '/borrow/images/081c8aa7-b514-4935-a9ea-bd698f378d9a',
              'thumbnails' => [
                'imageUrl' => '/borrow/images/82085a7b-b428-4c4b-b977-efbc2045ff46'
              ]
            }
          ],
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
