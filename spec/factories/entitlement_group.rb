class EntitlementGroup < Sequel::Model
  many_to_one(:inventory_pool)
  many_to_many(:users,
    left_key: :entitlement_group_id,
    right_key: :user_id,
    class: :User,
    join_table: :entitlement_groups_direct_users)
end

FactoryBot.define do
  factory :entitlement_group do
    created_at { Time.now }
    updated_at { Time.now }
    name { Faker::Name.last_name }
    inventory_pool
  end
end
