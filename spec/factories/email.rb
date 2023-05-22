class Email < Sequel::Model(:emails)
  many_to_one :user
end

# FactoryBot.define do
#   factory :email do
#     user
#     subject { Faker::Lorem.sentence }
#     body { Faker::Lorem.paragraph }
#     from_address { Faker::Internet.email }
#   end
# end
