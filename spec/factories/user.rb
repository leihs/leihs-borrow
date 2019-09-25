class User < Sequel::Model
end

FactoryBot.define do
  factory :user do
    firstname { Faker::Name.first_name }
    lastname { Faker::Name.last_name }
    email { Faker::Internet.email }
    ############################
    # migrate to column defaults
    created_at { Date.today }
    updated_at { Date.today }
    ############################

    after(:create) do |user|
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
