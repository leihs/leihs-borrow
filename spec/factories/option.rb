class Option < Sequel::Model(:options)
  many_to_one(:inventory_pool)
end

FactoryBot.define do
  factory :option do
    inventory_pool
    inventory_code do
      "#{Faker::Lorem.words(number: 3).join.slice(0, 3)}#{rand(9999) + 1000}"
    end
    manufacturer { nil }
    product { Faker::Commerce.product_name }
    price { rand(1500).round(2) }
  end
end
