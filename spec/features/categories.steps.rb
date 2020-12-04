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

step 'visit the sub-category :name' do |name|
  category = Category.find(name: name)
  c_seq = categories_seq(category)
  link = make_category_link(c_seq)
  visit(link)
end

def make_category_link(c_chain)
  "/app/borrow/categories/#{c_chain.map(&:id).join('/')}"
end

def categories_seq(category)
  prepend_parent = lambda do |c_chain|
    if p = c_chain.first.try(:parents).try(:first)
      [p] + c_chain
    else
      c_chain
    end
  end

  fix(prepend_parent).call([category])
end

def fix(f)
  l = lambda do |x|
    x2 = f.call(x)
    x2 == x ? x : l.call(x2)
  end
end
