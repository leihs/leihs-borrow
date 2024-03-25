step "I see one model with the title :name" do |name|
  expect(all(".ui-models-list-item").count).to eq 1
  find(".ui-models-list-item", text: name)
end

step "I click on the model with the title :name" do |name|
  find(".ui-models-list-item", text: name).click
end

step "the show page of the model :name was loaded" do |name|
  find("h1", text: name)
end

step "the order panel is shown" do
  find(".ui-booking-calendar")
end

step "the pools select box shows :name" do |name|
  expect(find("#pool-id").value).to eq InventoryPool.find(name: name).id
end

step "I name the order as :title" do |title|
  fill_in("title", with: title)
end
