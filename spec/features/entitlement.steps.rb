step 'I see the following models:' do |table|
  models_list = find('.ui-models-list')
  model_items = models_list.all('.ui-square-image-grid-item')
  captions = model_items.map {|item| item.find('.ui-caption', wait: false).text}

  expected_captions = table.hashes.map {|h| h['caption']}
  expect(captions).to eq expected_captions
end

step 'I enter :term in the main search field' do |term|
  within('.ui-model-search-filter') { fill_in(:term, with: term) }
end

step 'I press the enter key' do
  send_keys :enter
end

step "I visit the model show page of model :name" do |name|
  m = LeihsModel.find(product: name)
  visit "/app/borrow/models/#{m.id}"
end
