step 'I choose to filter by availabilty' do
  check('Show available only')
end

step 'I choose next working day as start date' do
  simulate_typing(find("input#start-date"),
                  Locales.format_date(Date.today, @user))
end

step 'I choose next next working day as end date' do
  simulate_typing(find("input#end-date"),
                  Locales.format_date(Date.tomorrow, @user))
end

step 'I set the quantity to :n' do |n|
  find("input[name='quantity']").set(n)
end
