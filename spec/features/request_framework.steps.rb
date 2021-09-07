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

step "I click on Add and I approve the dialog" do
  accept_alert { click_on("Hinzufügen") }
end

step "I click on retry and I approve the dialog" do
  accept_alert { click_on("BY CLICKING HERE") }
end

step "I see one retry banner" do
  within("section.bg-info") { expect(current_scope).to have_content "RETRY" }
end

step "I see one login banner" do
  banner = all("section form").first
  within(banner) do
    expect(current_scope).to have_selector("input#inputEmail")
    expect(current_scope).to have_selector("input#inputPassword")
  end
end

step "I login in via the login banner" do
  banner = all("section form").first
  within(banner) do
    find("input#inputEmail").set(@user.email)
    find("input#inputPassword").set("password")
    click_on("Submit")
  end
end

step "I don't see any retry banner" do
  expect(page).not_to have_selector("section.bg-info", text: /RETRY/)
end

step "I don't see any login banner" do
  expect(page).not_to have_selector("section form input#inputEmail")
  expect(page).not_to have_selector("section form input#inputPassword")
end

step "the cart is not empty" do
  visit("/app/borrow/order")
  expect(page).to have_content /1 Model\(s\), 1 Item\(s\)/
end

step "the order panel is shown" do
  find(".ui-booking-calendar")
end
