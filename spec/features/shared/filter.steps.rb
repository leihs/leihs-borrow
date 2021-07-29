step 'I choose to filter by availabilty' do
  check('Show available only')
end

step 'I choose next working day as start date' do
  fill_in('From', with: Date.today.to_s)
end

step 'I choose next next working day as end date' do
  fill_in('Until', with: Date.tomorrow.to_s)
end

step 'I set the quantity to :n' do |n|
  find("input[name='quantity']").set(n)
end
