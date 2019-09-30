class Model < Sequel::Model
  many_to_many(:categories,
               right_key: :model_group_id,
               join_table: :model_links)
  one_to_many(:items)
end

FactoryBot.define do
  factory :model do
    product { Faker::Commerce.product_name }

    transient do
      categories { [] }
      items { [] }
    end

    created_at { DateTime.now }
    updated_at { DateTime.now }

    after(:create) do |model, trans|
      trans.categories.each do |category|
        model.add_category(category)
      end

      trans.items.each do |item|
        model.add_item(item)
      end
    end
  end
end
