step 'I choose to filter by availabilty' do
  check('Select date (from/until)')
end

step 'I set the quantity to :n' do |n|
  find("input[name='quantity']").set(n)
end
