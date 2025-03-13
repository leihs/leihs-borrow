class Property < Sequel::Model
  many_to_one(:leihs_model, key: :model_id)
end

FactoryBot.define do
  factory :property do
    key { Faker::Hipster.words(number: 2).join(" ") }
    value { Faker::Measurement.metric_length }
  end
end
