class Workday < Sequel::Model
  many_to_one :inventory_pool
end
