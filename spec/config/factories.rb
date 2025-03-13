require "factory_bot"
require "faker"

Sequel::Model.db = database

FactoryBot.define do
  to_create { |instance| instance.save }
end

RSpec.configure do |config|
  config.include FactoryBot::Syntax::Methods

  config.before(:suite) do
    FactoryBot.find_definitions
  end
end
