step 'I choose to filter by availabilty' do
  check('Show available only')
end

step 'I set the quantity to :n' do |n|
  find("input[name='quantity']").set(n)
end
