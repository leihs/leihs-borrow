class PickupLocation < Sequel::Model
  many_to_one :inventory_pool
end

FactoryBot.define do
  factory :pickup_location do
    inventory_pool
    name { Faker::Lorem.word }
  end
end
