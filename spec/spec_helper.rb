require "active_support/all"
require "addressable"
require "base32/crockford"
require "uuidtools"
require "pry"

PROJECT_DIR = Pathname.new(__dir__).join("..")
require PROJECT_DIR.join("database/spec/config/database")
require "config/browser"
require "config/factories"
require "config/hash"
require "config/screenshots"
require "config/features"
require "config/locales"

RSpec.configure do |config|
  config.before(:example) do |example|
    srand 1
    db_clean
    db_restore_data seeds_sql
  end
end

def with_disabled_trigger(table, trigger)
  t_sql = (trigger == :all) ? "ALL" : trigger
  database.run "ALTER TABLE #{table} DISABLE TRIGGER #{t_sql}"
  result = yield
  database.run "ALTER TABLE #{table} ENABLE TRIGGER #{t_sql}"
  result
end
