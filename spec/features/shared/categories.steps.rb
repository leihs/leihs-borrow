step 'I click on category :category_name' do |category_name|
  find('.ui-models-list-item', text: category_name).click
end

step 'I click on sub-category :category_name' do |category_name|
  find('.ui-category-children a', text: category_name).click
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
