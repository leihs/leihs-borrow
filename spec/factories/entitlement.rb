class Entitlement < Sequel::Model
  many_to_one(:entitlement_group)
  many_to_one(:leihs_model, key: :model_id)
end

FactoryBot.define do
  factory :entitlement do
    #created_at { Time.now }
    #updated_at { Time.now }
    entitlement_group
    leihs_model
    #quantity
  end
end
