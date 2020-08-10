class SystemAdmin < Sequel::Model(:system_admin_users)
end

FactoryBot.define do
  factory :system_admin do
  end
end
