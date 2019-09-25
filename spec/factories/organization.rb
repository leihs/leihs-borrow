class Organization < Sequel::Model(:procurement_organizations)
  many_to_one :parent, class: Organization
end

FactoryBot.define do
  factory :department, class: Organization do
    name { Faker::Commerce.department }
  end

  factory :organization do
    name { Faker::Commerce.department }
    parent { create(:department) }
  end
end
