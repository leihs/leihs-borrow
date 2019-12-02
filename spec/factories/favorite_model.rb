class FavoriteModel < Sequel::Model
  many_to_one(:user)
  many_to_one(:leihs_model, key: :model_id)
end

FactoryBot.define do
  factory :favorite_model do
    user
    leihs_model
  end
end
