class InventoryPool < Sequel::Model
  def after_create
    Workday.create(inventory_pool_id: self.id,
                   saturday: true,
                   sunday: true)
    super
  end
end

FactoryBot.define do
  factory :inventory_pool do
    name { "#{Faker::Company.name} #{Faker::Company.suffix}" }
    shortname { name.split(' ').map { |s| s.slice(0) }.join }
    email { Faker::Internet.email }
  end
end
