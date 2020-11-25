step 'I choose to filter by availabilty' do
  # find('input[name="only-available"]').click
  find('label.custom-checkbox', text: "Show available only").click
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
