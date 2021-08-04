step 'I click on category :category_name' do |category_name|
  # NOTE: this a workaround for an apparent capybara/selenium/webdrive bug
  # using `click_on` throws an error:
  # Selenium::WebDriver::Error::ElementNotInteractableError: Element <a class="stretched-link" href="/app/borrow/categories/1234"> could not be scrolled into view
  # the trick is to not match a link but just some div which capybara will happily click 🙄
  
  # normally this should work:
  # within('section', text: 'Categories') { click_on(category_name) }
  
  within('section', text: 'Categories') do
    find('.ui-category-list > div', text: category_name).click
  end
end

step 'I click on sub-category :category_name' do |category_name|
  within('section', text: 'Unterkategorien') { click_on(category_name) }
end

step "don't see any breadcrumbs" do
  expect(page).not_to have_selector '.ui-category-breadcrumbs'
end

step 'I see the following breadcrumbs:' do |table|
  within '.ui-category-breadcrumbs' do
    table.rows.flatten.each do |str|
      expect(current_scope).to have_selector('a', text: str)
    end
  end
end
