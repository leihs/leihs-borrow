module Mock
  module Spec
    module FilterAvailable
      def self.availability(params)
        if params[:model_ids] == [
            'e5509e8e-95fc-4772-800c-dcdd8402789a',
            '4d15f802-47ad-4862-b595-1df259c118b6',
            'e63ea4dd-cd08-494e-864f-3092e63582e5',
            'b4bfcdb5-5e5e-47cd-a8a8-8ab0bb4222c1',
            '2821679a-90b0-498d-928e-82137b1ec3ed'
        ]
          [
            { model_id: 'e5509e8e-95fc-4772-800c-dcdd8402789a',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 1 },
            { model_id: '4d15f802-47ad-4862-b595-1df259c118b6',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: 'e63ea4dd-cd08-494e-864f-3092e63582e5',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: 'b4bfcdb5-5e5e-47cd-a8a8-8ab0bb4222c1',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: '2821679a-90b0-498d-928e-82137b1ec3ed',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 1 }
          ]
        elsif params[:model_ids] == [
              'e5fa8a1b-aeb6-4a9a-9b7c-9437f2edd244',
              '915fd455-1757-4888-a117-9719e64644c4',
              '317d9660-5eb1-4c38-b763-eca6e35ee20f',
              'eddd32a1-d465-430f-91ac-c26530cb0e13',
              '5fb44c5e-0027-4862-8d77-67422564d414'
        ]
          [
            { model_id: 'e5fa8a1b-aeb6-4a9a-9b7c-9437f2edd244',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 1 },
            { model_id: '915fd455-1757-4888-a117-9719e64644c4',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 1 },
            { model_id: '317d9660-5eb1-4c38-b763-eca6e35ee20f',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: 'eddd32a1-d465-430f-91ac-c26530cb0e13',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: '5fb44c5e-0027-4862-8d77-67422564d414',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 1 }
            ]
        elsif params[:model_ids] == [
          'ee367c0a-26ee-47aa-bdf7-55a4b7a36841',
          'b7dc6e69-9ab8-4e2b-87cb-30c16ec1dd6c',
          'd6536c1a-4977-4710-9055-34530e6edbfe',
          'cf032455-8443-4aa2-bd41-0ab374720b73',
          '2d70b519-1fa6-4213-b360-6ecffaf375fc'
        ]
          [
            { model_id: 'ee367c0a-26ee-47aa-bdf7-55a4b7a36841',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 1 },
            { model_id: 'b7dc6e69-9ab8-4e2b-87cb-30c16ec1dd6c',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 1 },
            { model_id: 'd6536c1a-4977-4710-9055-34530e6edbfe',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: 'cf032455-8443-4aa2-bd41-0ab374720b73',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: '2d70b519-1fa6-4213-b360-6ecffaf375fc',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 }
          ]
        elsif params[:model_ids] == [
          '3eafc404-a1c0-4482-b185-f3d73135cffa',
          '649bebbe-144a-4672-8f7f-131d1868d36b',
          'bea60deb-3074-43fb-a95b-55e404ac056c',
          '83b7eca5-1ad0-418b-b018-6a27046dc388',
          '65e85b5b-4b18-4a8f-8c4b-fff2e9cd374f'
        ]
          [
            { model_id: '3eafc404-a1c0-4482-b185-f3d73135cffa',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 1 },
            { model_id: '649bebbe-144a-4672-8f7f-131d1868d36b',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: 'bea60deb-3074-43fb-a95b-55e404ac056c',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: '83b7eca5-1ad0-418b-b018-6a27046dc388',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: '65e85b5b-4b18-4a8f-8c4b-fff2e9cd374f',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 }
          ]
        elsif params[:model_ids] == [
          '6ab9d55e-b806-40d9-ad10-8fb94361568d',
          'fb502498-7391-4bdd-9353-11daa55b1cc3',
          'a61af742-f807-42f7-a917-f390eebc139d',
          '11327f34-ccf9-405d-b099-5abfd9d73596',
          'b14656a7-019f-40d7-b27d-c46be0c88df6'
        ]
          [
            { model_id: '6ab9d55e-b806-40d9-ad10-8fb94361568d',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: 'fb502498-7391-4bdd-9353-11daa55b1cc3',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: 'a61af742-f807-42f7-a917-f390eebc139d',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 1 },
            { model_id: '11327f34-ccf9-405d-b099-5abfd9d73596',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: 'b14656a7-019f-40d7-b27d-c46be0c88df6',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 }
          ]
        elsif params[:model_ids] == [
          '589435ab-64e6-497e-a177-2b7a3a00aef7',
          '5373b0d3-4b7c-4223-9e48-98f161e6d6d8',
          '0212710d-e152-4020-8b95-e53cf5c748e9',
          'a402c1e4-5c7e-4108-bcb7-e0cae3602612',
          'db3f1cb0-aa2d-4148-b9f5-adb2a0d208af'
        ]
          [
            { model_id: '589435ab-64e6-497e-a177-2b7a3a00aef7',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: '5373b0d3-4b7c-4223-9e48-98f161e6d6d8',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: '0212710d-e152-4020-8b95-e53cf5c748e9',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: 'a402c1e4-5c7e-4108-bcb7-e0cae3602612',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 0 },
            { model_id: 'db3f1cb0-aa2d-4148-b9f5-adb2a0d208af',
              inventory_pool_id: 'f335cd67-9b1e-48ee-a2f9-e265b91dc58d',
              quantity: 1 }
          ]
        end
      end
    end
  end
end
