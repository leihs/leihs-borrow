class Template < Sequel::Model(:model_groups)
  many_to_many(:direct_models,
    class: :LeihsModel,
    left_key: :model_group_id,
    right_key: :model_id,
    join_table: :model_links)

  many_to_many(:inventory_pools,
    class: :InventoryPool,
    left_key: :model_group_id,
    right_key: :inventory_pool_id,
    join_table: :inventory_pools_model_groups)
end

FactoryBot.define do
  factory :template do
    name { Faker::Marketing.buzzwords }
    type { "Template" }

    created_at { DateTime.now }
    updated_at { DateTime.now }

    transient do
      direct_models { [] }
    end

    after(:create) do |template, trans|
      trans.direct_models.each do |model|
        template.add_direct_model(model)
      end
    end
  end
end
