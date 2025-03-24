class InventoryPool < Sequel::Model
  one_to_one(:workday)
end

FactoryBot.define do
  factory :inventory_pool do
    name { "#{Faker::Company.name} #{Faker::Company.suffix}" }
    shortname { name.split(" ").map { |s| s.slice(0) }.join }
    email { Faker::Internet.email }

    after(:create) do |ip|
      ip.workday.update(saturday: true, sunday: true,
        saturday_orders_processing: true, sunday_orders_processing: true)
    end
  end
end
