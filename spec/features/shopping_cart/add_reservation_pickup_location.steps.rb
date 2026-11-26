step "I visit the show page of model :name" do |name|
  model = LeihsModel.find(product: name) || fail("Model not found: #{name.inspect}")
  visit "/borrow/models/#{model.id}"
end

step "the :section_title section links :link_text to the page of pool :pool_name" do |section_title, link_text, pool_name|
  pool = InventoryPool.find(name: pool_name) || fail("Pool not found: #{pool_name.inspect}")
  within(find_ui_section(title: section_title)) do
    expect(find("a", text: link_text)[:href]).to end_with "/borrow/inventory-pools/#{pool.id}"
  end
end

step "the newly created reservation in the DB has the pickup location :location_name" do |location_name|
  reservation = Reservation.order(Sequel.desc(:created_at)).first
  expect(reservation).to be
  location = PickupLocation[reservation.pickup_location_id]
  expect(location&.name).to eq location_name
end

step "the newly created reservation in the DB has no pickup location" do
  reservation = Reservation.order(Sequel.desc(:created_at)).first
  expect(reservation).to be
  expect(reservation.pickup_location_id).to be_nil
end

# "Add" alone is ambiguous: the model page behind the dialog has "Add item"
# and "Add to favorites".
#
# The order dialog blocks submitting through the "disabled" CSS class rather than
# the attribute (the attribute only reflects whether a save is in flight).
step "the :label button of the dialog is disabled" do |label|
  expect(@dialog).to be
  button = @dialog.find("button", text: label, exact_text: true)
  expect(button.disabled? || button[:class].to_s.split.include?("disabled")).to be true
end
