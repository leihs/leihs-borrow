class User < Sequel::Model
  one_to_many(:direct_access_rights)
  many_to_one(:delegator_user, class: self)
  many_to_many(:delegation_users,
    left_key: :delegation_id,
    right_key: :user_id,
    class: self,
    join_table: :delegations_users)
  many_to_many(:favorite_models,
    class: LeihsModel,
    join_table: :favorite_models,
    left_key: :user_id,
    right_key: :model_id)
  many_to_one(:language, key: :language_locale)
end

FactoryBot.define do
  factory :user_base, class: User do
    created_at { Date.today }
    updated_at { Date.today }
    email { Faker::Internet.email }
    language do
      Language.find(locale: "en-GB") or
        create(:language,
          locale: "en-GB",
          name: "British English",
          default: true)
    end
    organization { Faker::Lorem.characters(number: 8) }

    transient do
      access_rights { [] }
    end

    after(:create) do |user, trans|
      trans.access_rights.each do |access_right|
        user.add_direct_access_right(access_right)
      end
    end

    factory :delegation do
      transient do
        name { Faker::Team.name }
        responsible { create(:user) }
        members { [] }
      end

      after(:build) do |user, trans|
        user.firstname = trans.name
        user.delegator_user = trans.responsible
      end

      after(:create) do |user, trans|
        trans.members.each do |member|
          user.add_delegation_user(member)
        end
      end
    end

    factory :user do
      firstname { Faker::Name.first_name }
      lastname { Faker::Name.last_name }

      after(:create) do |user, trans|
        pw_hash = database[<<-SQL]
          SELECT crypt(
            #{database.literal("password")},
            gen_salt('bf')
          ) AS pw_hash
        SQL
          .first[:pw_hash]

        database[:authentication_systems_users].insert(
          user_id: user.id,
          authentication_system_id: "password",
          data: pw_hash
        )
      end
    end
  end
end
