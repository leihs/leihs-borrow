class Supplier < Sequel::Model
end

FactoryBot.define do
  factory :supplier do
    name { Faker::Company.name }
  end
end
