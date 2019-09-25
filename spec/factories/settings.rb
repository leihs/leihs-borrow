class Settings < Sequel::Model(:procurement_settings)
end

FactoryBot.define do
  factory :settings do
    contact_url { Faker::Internet.url }
    inspection_comments do
      (1..3)
        .map { |_| Faker::Lorem.sentence }
        .to_json
    end
  end
end
