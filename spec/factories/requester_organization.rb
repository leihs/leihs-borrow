class RequesterOrganization < Sequel::Model(:procurement_requesters_organizations)
  many_to_one :user
  many_to_one :organization
end

FactoryBot.define do
  factory :requester_organization do
    user_id { create(:user).id }
    organization_id { create(:organization).id }
  end
end
