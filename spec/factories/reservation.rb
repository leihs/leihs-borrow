class Reservation < Sequel::Model
  many_to_one(:order)
  many_to_one(:contract)
  many_to_one(:user)
  many_to_one(:inventory_pool)
  many_to_one(:item)
  many_to_one(:leihs_model, key: :model_id)
  many_to_one(:option)
end

FactoryBot.define do
  factory :reservation do
    user
    inventory_pool
    leihs_model
    start_date { Date.tomorrow.to_s }
    end_date { (Date.tomorrow + 1.day).to_s }
    status { "unsubmitted" }
    created_at { DateTime.now }
    updated_at { DateTime.now }
  end
end
