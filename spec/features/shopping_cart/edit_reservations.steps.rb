step "the :name field has :value" do |name, value|
  expect(find_field(name).value).to eq value.to_s
end

step "I wait :num seconds" do |time|
  sleep time.to_f
end
