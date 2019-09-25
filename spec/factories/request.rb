class Request < Sequel::Model(:procurement_requests)
  UUID_ATTRS = Set[
    :budget_period,
    :category,
    :model,
    :organization,
    :room,
    :supplier,
    :user
  ]
end

FactoryBot.define do
  factory :request do
    article_name { Faker::Commerce.product_name }
    budget_period_id { create(:budget_period).id }
    category_id { create(:category).id }
    organization_id { create(:organization).id }
    requested_quantity { rand(50) }
    room_id { create(:room).id }
    user_id { create(:user).id }
    motivation { Faker::Lorem.sentence }
    supplier_name { Faker::Company.name }

    after :build do |req|
      if template = Template.find(id: req.template_id)
        req.article_name = template.article_name
        req.article_number = template.article_number
        req.model_id = template.model_id
        req.price_cents = template.price_cents
        req.supplier_id = template.supplier_id
        req.supplier_name = template.supplier_name
      end
    end
  end
end
