require 'spec_helper'

describe 'factories' do
  it 'works' do
    ip = FactoryBot.create(:inventory_pool)
    u = FactoryBot.create(:user)
    binding.pry if ENV['PRY']
  end
end
