class Room < Sequel::Model
end

FactoryBot.define do
  factory :room do
    name { Faker::Address.building_number }
    building_id { create(:building).id }
  end

  trait :general_from_general do
    building_id { create(:building, :general).id }
    general true
  end
end
