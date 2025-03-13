class PoolOrder < Sequel::Model(:orders)
  many_to_one(:user)
  many_to_one(:inventory_pool)
  many_to_one(:order, key: :customer_order_id)
  one_to_many(:reservations, key: :order_id)
end

FactoryBot.define do
  factory :pool_order do
    order
    user
    inventory_pool
    state { "submitted" }
    purpose { Faker::Lorem.sentence }
  end
end
