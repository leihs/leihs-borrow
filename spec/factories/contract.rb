class Contract < Sequel::Model
  many_to_one(:user)
  many_to_one(:inventory_pool)
  one_to_many(:reservations)
end

FactoryBot.define do
  factory :contract do
    user
    inventory_pool
    purpose { Faker::Lorem.sentence }
    created_at { DateTime.now } 
    updated_at { DateTime.now }

    transient do
      uuid { UUIDTools::UUID.random_create }
    end

    after(:build) do |contract, trans|
      contract.id = trans.uuid.to_s
      b32 = Base32::Crockford.encode(contract.id.to_i)
      contract.compact_id ||= \
        (3..26)
        .lazy
        .map { |i| b32[0..i] }
        .map { |c_id| !Contract.find(compact_id: c_id) && c_id }
        .find(&:itself)
    end
  end
end
