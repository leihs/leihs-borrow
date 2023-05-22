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

    trait :with_mail_templates do
      after(:create) do |pool|
        MailTemplate.all.each do |mt|
          attrs =
            mt.to_hash
            .reject { |k, _| [:id, :is_template_template].include?(k) }
            .merge(inventory_pool_id: pool.id, is_template_template: false)
          MailTemplate.create(attrs)
        end
      end
    end
  end
end
