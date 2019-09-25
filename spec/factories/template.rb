class Template < Sequel::Model(:procurement_templates)
end

FactoryBot.define do
  factory :template, class: Template do
    article_name { Faker::Commerce.product_name }
    category_id { create(:category).id }
  end
end
