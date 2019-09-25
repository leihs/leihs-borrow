class Model < Sequel::Model
end

FactoryBot.define do
  factory :model do
    product { Faker::Commerce.product_name }
  end
end
