require 'spec_helper'
require 'pry'
require_relative '../shared/common.rb'

feature 'abort queries' do
  scenario 'main' do
    pending "Query abort is currently disabled, see issue #1294"

    @user = FactoryBot.create(:user)
    login(@user)
    visit "/app/borrow/testing/step-1"
    wait_until { parse_pre("#requests").empty? } 
    click_on "mutate"
    mutation_id = parse_pre("#requests").first.first
    click_on "query"
    query_id = parse_pre("#requests").select { |k, _| k != mutation_id }.first.first
    click_on "-> Step 2"
    req_ids = parse_pre("#requests").keys
    expect(req_ids).to include mutation_id
    expect(req_ids).not_to include query_id
  end
end

def parse_pre(id)
  JSON.parse find(id).text
end

def login(user)
  visit '/app/borrow/'
  within('.ui-form-signin') do
    fill_in 'user', with: user.email
    find('button[type="submit"]').click
  end
  within('.ui-form-signin') do
    fill_in 'password', with: 'password'
    find('button[type="submit"]').click
  end
end
