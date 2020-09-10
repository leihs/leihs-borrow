class Language < Sequel::Model
end

FactoryBot.define do
  factory :language do
    active { true }
    default { false }
  end
end
