class Contract < Sequel::Model
  many_to_one(:user)
  many_to_one(:inventory_pool)
  one_to_many(:reservations)

  def self.create_with_disabled_triggers(id,
                                         user_id,
                                         inventory_pool_id,
                                         state = :open,
                                         compact_id = id,
                                         purpose = Faker::Lorem.word)
    with_disabled_triggers do
      database.run <<-SQL
        INSERT INTO contracts(
          id,
          user_id,
          inventory_pool_id,
          compact_id,
          purpose,
          created_at,
          updated_at,
          state
        )
        VALUES (
          '#{id}',
          '#{user_id}',
          '#{inventory_pool_id}',
          '#{compact_id}',
          '#{purpose}',
          now(),
          now(),
          '#{state}'
          );
      SQL
    end

    def self.update_with_disabled_triggers(id, column, value)
      with_disabled_triggers do
        database.run <<-SQL
          UPDATE contracts SET #{column} = #{value} WHERE id = '#{id}'
        SQL
      end
    end

    find(id: id) 
  end
end

FactoryBot.define do
  factory :contract do
    user
    inventory_pool
    purpose { Faker::Lorem.sentence }
    created_at { DateTime.now } 
    updated_at { DateTime.now }

    transient do
      uuid { UUIDTools::UUID.random_create }
    end

    after(:build) do |contract, trans|
      contract.id = trans.uuid.to_s
      b32 = Base32::Crockford.encode(contract.id.to_i)
      contract.compact_id ||= \
        (3..26)
        .lazy
        .map { |i| b32[0..i] }
        .map { |c_id| !Contract.find(compact_id: c_id) && c_id }
        .find(&:itself)
    end
  end
end
