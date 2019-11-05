class Order < Sequel::Model(:customer_orders)
  many_to_one(:user)
  one_to_many(:orders, key: :customer_order_id)
end

FactoryBot.define do
  factory :order do
    user
  end
end
