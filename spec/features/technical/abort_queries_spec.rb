require 'spec_helper'
require 'pry'

feature 'abort queries' do
  scenario 'main' do
    @user = FactoryBot.create(:user)
    login(@user)
    # When I log in as the user
    # And I visit "/app/borrow/testing/step-1"
    # And I click on "query"
    # And I click on "mutate"
    # Then I see 2 running requests
    # And I see 1 running mutation id
    # When I click on "-> Step 2"
    # And I wait for 1 second
  end
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
