class Model < Sequel::Model
  many_to_many(:categories,
               right_key: :model_group_id,
               join_table: :model_links)
  one_to_many(:items)
  one_to_many(:images, key: :target_id)
end

FactoryBot.define do
  factory :model do
    product { Faker::Commerce.product_name }

    transient do
      categories { [] }
      items { [] }
      images { [] }
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

      trans.images.each do |image|
        model.add_image(image)
      end
    end
  end
end
