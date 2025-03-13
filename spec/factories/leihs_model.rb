class LeihsModel < Sequel::Model(:models)
  many_to_many(:categories,
    left_key: :model_id,
    right_key: :model_group_id,
    join_table: :model_links)
  one_to_many(:items, key: :model_id)
  one_to_many(:images, key: :target_id)
  one_to_many(:attachments, key: :model_id)
  one_to_many(:properties, key: :model_id)
  many_to_many(:recommends,
    class: :LeihsModel,
    left_key: :model_id,
    right_key: :compatible_id,
    join_table: :models_compatibles)
end

FactoryBot.define do
  factory :leihs_model do
    product { Faker::Commerce.product_name }

    transient do
      categories { [] }
      items { [] }
      images { [] }
      properties { [] }
      recommends { [] }
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

      trans.properties.each do |property|
        model.add_property(property)
      end

      trans.recommends.each do |recommend|
        model.add_recommend(recommend)
      end
    end
  end
end
