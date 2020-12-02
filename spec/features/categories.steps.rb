step 'I see :n model(s)' do |n|
  find('.ui-models-list-item', match: :first)
  model_items = all('.ui-models-list-item')
  expect(model_items.count).to eq n.to_i
end

step 'I see model :model_name' do |model_name|
  find('.ui-models-list-item', text: model_name, match: :prefer_exact)
end

step 'I click on :name within breadcrumbs' do |name|
  within '#breadcrumbs' do
    click_on(name)
  end
end
