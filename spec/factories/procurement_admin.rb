class ProcurementAdmin < Sequel::Model
  many_to_one :user
end

FactoryBot.define do
  factory :procurement_admin do
    user
  end
end
