class Item < Sequel::Model
  many_to_one(:leihs_model, class: :Model, key: :model_id)
  many_to_one(:responsible, class: :InventoryPool, key: :inventory_pool_id)
  many_to_one(:owner, class: :InventoryPool, key: :owner_id)
  many_to_one(:room)
end

FactoryBot.define do
  factory :item do
    inventory_code { Faker::Alphanumeric.alphanumeric(number: 10) }
    association :leihs_model, factory: :model 
    association :owner, factory: :inventory_pool
    association :responsible, factory: :inventory_pool
    room
    
    created_at { DateTime.now }
    updated_at { DateTime.now }
  end
end