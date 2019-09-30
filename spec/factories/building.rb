class Building < Sequel::Model
end

FactoryBot.define do
  factory :building do
    name { Faker::Address.street_address }
  end
end
