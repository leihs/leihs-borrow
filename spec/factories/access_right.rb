class AccessRight < Sequel::Model
  many_to_one(:user)
  many_to_one(:inventory_pool)
end

FactoryBot.define do
  factory :access_right do
    inventory_pool
    user
    role { :customer }
  end
end
