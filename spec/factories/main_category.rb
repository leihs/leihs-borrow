class MainCategory < Sequel::Model(:procurement_main_categories)
end

FactoryBot.define do
  factory :main_category do
    name { "#{Faker::Cat.name} #{Faker::Cat.breed}" }
  end
end
