class Suspension < Sequel::Model
  many_to_one(:user)
  many_to_one(:inventory_pool)
end

FactoryBot.define do
  factory :suspension do
    user
    inventory_pool
    suspended_until { "2099-01-01" }
    suspended_reason { "Wegen Überfälliger Rückgabe automatisch gesperrt!" }
  end
end
