step 'I set the quantity to :n' do |n|
  find("input[name='quantity']").set(n)
end
