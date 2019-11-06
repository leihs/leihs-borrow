require 'spec_helper'
require_relative 'graphql_helper'

describe 'models' do
  it 'works' do
    user = FactoryBot.create(:user,
                             id: '4dc6adb4-ed7c-46cb-8573-8e585f70f4de')
    inventory_pool = FactoryBot.create(:inventory_pool,
                                       id: '232547a5-5f43-450c-896a-b692275a04ea')
    FactoryBot.create(:access_right,
                      inventory_pool: inventory_pool,
                      user: user)

    recommend = FactoryBot.create(
      :leihs_model,
      id: '210a4116-162f-4947-bcb0-2d7d1a5c7b1c',
      items: [
        FactoryBot.create(
          :item,
          responsible: inventory_pool,
          is_borrowable: true
        )
      ]
    )

    model = FactoryBot.create(
      :leihs_model,
      id: '2bc1deb5-9428-4178-afd0-c06bb8d31ff3',
      items: [
        FactoryBot.create(
          :item,
          responsible: inventory_pool,
          is_borrowable: true
        )
      ],
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
      ],
      recommends: [recommend]
    )

    model.add_attachment(
      FactoryBot.build(
        :attachment,
        id: '919fbdd1-111c-49b7-aeb0-2d5d8825ed00'
      )
    )

    q = <<-GRAPHQL
      {
        models(
          orderBy: [{attribute: ID, direction: ASC}],
          startDate: "2019-10-24",
          endDate: "2019-10-25",
          inventoryPoolIds: ["232547a5-5f43-450c-896a-b692275a04ea"]
        ) {
          id
          images {
            imageUrl
          }
          attachments {
            url
          }
          properties {
            id
          }
          recommends {
            id
          }
          availability {
            inventoryPool {
              id
            }
            dates {
              date
              quantity
            }
          }
        }
      }
    GRAPHQL

    expect_graphql_result(
      query(q, user.id),
      {
        models: [
          {
            # recommend
            id: '210a4116-162f-4947-bcb0-2d7d1a5c7b1c',
            images: [],
            attachments: [],
            properties: [],
            recommends: [],
            availability: [
              {
                inventoryPool: { id: '232547a5-5f43-450c-896a-b692275a04ea' },
                dates: [{ date: '2019-10-24', quantity: 1 }, { date: '2019-10-25', quantity: 1 }]
              }
            ]
          },
          {
            # model of interest
            id: '2bc1deb5-9428-4178-afd0-c06bb8d31ff3',
            images: [{ imageUrl: '/borrow/images/7484b5d2-376a-4b15-8db0-54cc6bab02ea' }],
            attachments: [{ url: '/borrow/attachments/919fbdd1-111c-49b7-aeb0-2d5d8825ed00' }],
            properties: [{ id: '2df736a4-825c-4f36-b48a-75875b3a3c26' }],
            recommends: [{ id: '210a4116-162f-4947-bcb0-2d7d1a5c7b1c' }],
            availability: [
              {
                inventoryPool: { id: '232547a5-5f43-450c-896a-b692275a04ea' },
                dates: [{ date: '2019-10-24', quantity: 1 }, { date: '2019-10-25', quantity: 1 }]
              }
            ]
          }
        ]
      }
    )
  end
end
