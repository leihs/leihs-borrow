step 'I click on category :category_name' do |category_name|
  find('.ui-models-list-item', text: category_name).click
end

step 'I click on sub-category :category_name' do |category_name|
  find("#children a", text: category_name).click
end

step "don't see any breadcrumbs" do
  expect(page).not_to have_selector '#breadcrumbs'
end

step 'I see the following breadcrumb:' do |table|
  within '#breadcrumbs' do
    expect(current_scope).to have_content table.rows.flatten.join(" | ")
  end
end
