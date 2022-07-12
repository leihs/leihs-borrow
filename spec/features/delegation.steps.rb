step "I see one model with the title :name" do |name|
  expect(all(".ui-models-list-item").count).to eq 1
  find(".ui-models-list-item", text: name)
end

step "the order panel is shown" do
  find(".ui-booking-calendar")
end

step "the :title button is disabled" do |title|
  expect(find("button", text: title)).to be_disabled
end

step "I switch to a new window" do
  w = open_new_window()
  switch_to_window(w)
end

step "I go back to the first window" do
  switch_to_window(windows.first)
end
