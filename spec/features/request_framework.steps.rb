step "there is a borrowable item in pool :name" do |name|
  pool = InventoryPool.find(name: name)
  @item = FactoryBot.create(:item, inventory_pool_id: pool.id)
end

step "I visit the model show page for the borrowable item" do
  visit "/app/borrow/models/#{@item.model_id}"
end

step "I clear the browser cookies" do
  browser = Capybara.current_session.driver.browser
  browser.manage.delete_cookie("leihs-user-session")
end

step "the cart is not empty" do
  visit("/app/borrow/order")
  step "I see the 'Items' section"
end

step "the order panel is shown" do
  find(".ui-booking-calendar")
end

step "I log in again" do
  expect(@user).to be_a User
  within(".ui-form-signin") do
    fill_in("user", with: @user.email)
    find('button[type="submit"]').click
  end
  within(".ui-form-signin") do
    fill_in("password", with: "password")
    find('button[type="submit"]').click
  end
end
