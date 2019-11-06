class User < Sequel::Model
  one_to_many(:access_rights)
  many_to_one(:delegator_user, class: self)
  many_to_many(:delegation_users,
               left_key: :delegation_id,
               right_key: :user_id,
               class: self,
               join_table: :delegations_users)
end

FactoryBot.define do
  factory :user_base, class: User do
    created_at { Date.today }
    updated_at { Date.today }
    email { Faker::Internet.email }

    transient do
      access_rights { [] }
    end

    after(:create) do |user, trans|
      trans.access_rights.each do |access_right|
        user.add_access_right(access_right)
      end
    end

    factory :delegation do
      firstname { Faker::Team.name }
      delegator_user { create(:user) }

      transient do 
        delegated_users { [] }
      end

      after(:create) do |user, trans|
        trans.delegated_users.each do |delegated_user|
          user.add_delegated_user(delegated_user)
        end
      end
    end

    factory :user do
      firstname { Faker::Name.first_name }
      lastname { Faker::Name.last_name }

      after(:create) do |user, trans|
        pw_hash = database[<<-SQL]
          SELECT crypt(
            #{database.literal('password')},
            gen_salt('bf')
          ) AS pw_hash
        SQL
          .first[:pw_hash]

        database[:authentication_systems_users].insert(
          user_id: user.id, 
          authentication_system_id: 'password',
          data: pw_hash
        )
      end
    end
  end
end
