class UserFilter < Sequel::Model(:procurement_users_filters)
end

FactoryBot.define do
  factory :user_filter do
    user_id { create(:user).id }
    filter Hash[
      :sort_by, :state,
      :sort_dir, :asc,
      :search, "",
      :budget_period_ids, [],
      :category_ids, [],
      :organization_ids, [],
      :priorities, [:high, :normal],
      :inspector_priorities, [:mandatory, :high, :medium, :low],
      :states, [:new, :approved, :partially_approved, :denied]
    ].to_json
  end
end
