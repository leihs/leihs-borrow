class Reservation < Sequel::Model
  many_to_one(:order)
  many_to_one(:contract)
  many_to_one(:user)
  many_to_one(:inventory_pool)
  many_to_one(:leihs_model, key: :model_id)
end

FactoryBot.define do
  factory :reservation do
    user
    inventory_pool
    leihs_model
    status { 'unsubmitted' }
    created_at { DateTime.now } 
    updated_at { DateTime.now }
  end
end
