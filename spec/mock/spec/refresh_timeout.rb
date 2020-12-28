module Mock
  module Spec
    module RefreshTimeout
      def self.availability(params)
        if params[:model_ids] == ['db3197f4-7fef-4139-83e1-09f79abfa691']
          [
            { model_id: 'db3197f4-7fef-4139-83e1-09f79abfa691',
              inventory_pool_id: '00843766-b48d-4a7d-89cc-565ced81bbf9',
              quantity: 2 },
          ]
        elsif params[:model_ids] == ['a95ee25f-37cb-4c85-8efd-40cead86396e']
          [
            { model_id: 'a95ee25f-37cb-4c85-8efd-40cead86396e',
              inventory_pool_id: '00843766-b48d-4a7d-89cc-565ced81bbf9',
              quantity: 0 },
          ]
        end
      end
    end
  end
end
