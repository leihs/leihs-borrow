step 'there are :n different :model models with a borrowable items in :pool' do |n, model_name, pool|
  pool = InventoryPool.find(name: pool)
  (1..n.to_i).each do |i|
    model = FactoryBot.create(:leihs_model, product: "#{model_name} #{i}")
    FactoryBot.create(
      :item,
      is_borrowable: true, leihs_model: model, responsible: pool, owner: pool
    )
  end
end

step 'the model :model_name has 1 borrowable item in :pool_name' do |model_name, pool_name|
  pool = InventoryPool.find(name: pool_name)
  model = LeihsModel.find(product: model_name)
  FactoryBot.create(
    :item,
    is_borrowable: true, leihs_model: model, responsible: pool, owner: pool
  )
end

step 'there is a subcategory :subcat in :cat' do |subcat, cat|
  parent_cat = Category.find(name: cat)
  FactoryBot.create(:category, name: subcat, parents: [parent_cat])
end

step 'the model :model_name belongs to the :cat_name category' do |model_name, cat_name|
  model = LeihsModel.find(product: model_name)
  category = Category.find(name: cat_name)
  model.add_category(category)
end

step 'I see :n (different) :model model(s)' do |n, model_name|
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
  n.to_i.times do
    click_on(txt)
    sleep(1)
  end
end

step 'there is :txt button' do |txt|
  expect(page).to have_content txt
end

step 'there is no :txt button' do |txt|
  expect(page).not_to have_content txt
end

step 'I go to the homepage' do
  within('.ui-main-nav') { click_on('LEIHS') }
end

step 'I see category :category_name' do |category_name|
  within('section', text: 'Categories') do
    expect(find('a', text: category_name))
  end
end

step 'I enter :term in the search field' do |term|
  fill_in('Search', with: term)
end

step 'I enter :term in the main search field' do |term|
  within('.ui-model-search-filter') { fill_in(:term, with: term) }
end

step 'I select pool :pool_name' do |pool_name|
  select(pool_name, from: 'pool-id')
end

step 'I select all pools' do
  select('All', from: 'pool-id')
end

step 'there are no results' do
  expect(page).not_to have_selector '.square-container'
end

step "I don't see any :model_name model" do |model_name|
  expect(page).not_to have_content model_name
end

step 'the search field contains :term' do |term|
  expect(find_field('term').value).to eq term
end

step 'I press the enter key' do
  send_keys :enter
end
