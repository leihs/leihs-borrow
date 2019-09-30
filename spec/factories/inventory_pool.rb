class InventoryPool < Sequel::Model
end

FactoryBot.define do
  factory :inventory_pool do
    name { "#{Faker::Company.name} #{Faker::Company.suffix}" }
    shortname { name.split(' ').map { |s| s.slice(0) }.join }
    email { Faker::Internet.email }
  end
end
