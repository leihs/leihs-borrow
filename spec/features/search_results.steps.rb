step 'there are :n different :model models with a borrowable items in :pool' do |n, model_name, pool|
  pool = InventoryPool.find(name: pool)
  (1..n.to_i).each do |i|
    model = FactoryBot.create(:leihs_model, product: "#{model_name} #{i}")
    FactoryBot.create(:item,
                      is_borrowable: true,
                      leihs_model: model,
                      responsible: pool,
                      owner: pool)
  end
end

step 'I see :n different :model models' do |n, model_name|
  find('.ui-models-list-item', match: :first)
  model_items = all('.ui-models-list-item', text: /#{model_name}/)
  expect(model_items.count).to eq n.to_i
end

step 'all the :model_name models belong to the :category category' do |model_name, category_name|
  models = LeihsModel.all
  category = Category.find(name: category_name)
  category ||= FactoryBot.create(:category, name: category_name)
  models2 = models.select { |m| m.product =~ /#{model_name}/ }
  models2.each { |m| m.add_category(category) }
end

step 'I click :n times on :txt' do |n, txt|
  n.to_i.times { click_on(txt); sleep(1) }
end

step 'there is :txt button' do |txt|
  expect(page).to have_content txt
end

step 'there is no :txt button' do |txt|
  expect(page).not_to have_content txt
end

step 'I click on category :category_name' do |category_name|
  find('.ui-models-list-item', text: category_name).click
end

step 'I see category :category_name' do |category_name|
  find('.ui-models-list-item', text: category_name)
end

step 'I enter :term in the search field' do |term|
  fill_in('Search', with: term)
end

step 'I select pool :pool_name' do |pool_name|
  select(pool_name, from: 'From')
end

step 'I select all pools' do
  select('All inventory pools', from: 'From')
end

step 'there are no results' do
  expect(page).not_to have_selector '.ui-models-list-item'
end

step 'I don\'t see any :model_name model' do |model_name|
  expect(page).not_to have_content model_name
end
