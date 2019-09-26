class Category < Sequel::Model(:model_groups)
  many_to_many(:parents, 
               class: self,
               left_key: :child_id,
               right_key: :parent_id,
               join_table: :model_group_links)

  many_to_many(:children, 
               class: self,
               left_key: :parent_id,
               right_key: :child_id,
               join_table: :model_group_links)

  many_to_many(:direct_models,
               class: :Model,
               left_key: :model_group_id,
               right_key: :model_id,
               join_table: :model_links)
end

FactoryBot.define do
  factory :category do
    name { Faker::Commerce.department(max: 2) }
    type { 'Category' }

    created_at { DateTime.now }
    updated_at { DateTime.now }

    transient do
      parents { [] }
      children { [] }
      direct_models { [] }
    end

    after(:create) do |category, trans|
      trans.parents.each do |parent|
        category.add_parent(parent)
      end

      trans.children.each do |child|
        category.add_child(child)
      end

      trans.direct_models.each do |model|
        category.add_direct_model(model)
      end
    end
  end
end
