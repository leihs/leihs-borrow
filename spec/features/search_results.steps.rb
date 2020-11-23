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

step 'I click :n times on :txt' do |n, txt|
  n.to_i.times { click_on(txt); sleep(1) }
end

step 'there is no :txt button' do |txt|
  expect(page).not_to have_content txt
end
