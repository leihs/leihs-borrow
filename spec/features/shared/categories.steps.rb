step "I click on category :category_name" do |category_name|
  # NOTE: this a workaround for an apparent capybara/selenium/webdrive bug
  # using `click_on` throws an error:
  # Selenium::WebDriver::Error::ElementNotInteractableError: Element <a class="stretched-link" href="/app/borrow/categories/1234"> could not be scrolled into view
  # the trick is to not match a link but just some div which capybara will happily click 🙄

  # normally this should work:
  # within('section', text: 'Categories') { click_on(category_name) }

  within("section", text: "Categories") do
    find(".ui-category-list > div", text: category_name).click
  end
end

step "I expand the :name section" do |name|
  within("section", text: name) { find(".ui-section-expander").click }
end

step "I collapse the :name section" do |name|
  within("section", text: name) { find(".ui-section-collapser").click }
end

step "the :name section is expanded" do |name|
  within("section", text: name) { find(".ui-section-collapser") }
end

step "the :name section is collapsed" do |name|
  within("section", text: name) { find(".ui-section-expander") }
end

step "I click on sub-category :category_name" do |category_name|
  within("section", text: "Sub-categories") { click_on(category_name) }
end

step "don't see any breadcrumbs" do
  expect(page).not_to have_selector ".ui-category-breadcrumbs"
end

step "I see the following breadcrumbs:" do |table|
  within ".ui-category-breadcrumbs" do
    table.rows.flatten.each do |str|
      expect(current_scope).to have_selector("a", text: str)
    end
  end
end
