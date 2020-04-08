class Workday < Sequel::Model
  many_to_one :inventory_pool
end

FactoryBot.define do
  factory :workday do
    inventory_pool
    monday { true }
    tuesday { true }
    wednesday { true }
    thursday { true }
    friday { true }
    saturday { true }
    sunday { true }
  end
end
