step "I visit the model show page of model :name" do |name|
  m = LeihsModel.find(product: name) || fail("Model not found: #{name.inspect}")
  visit "/borrow/models/#{m.id}"
end

step "the pickup location select is not shown" do
  expect(page).to have_no_selector("#pickup-location-id")
end

step "the pickup location select is shown" do
  expect(page).to have_selector("#pickup-location-id")
end

step "the unsubmitted reservation for model :name has no pickup location" do |name|
  model = LeihsModel.find(product: name) || fail("Model not found: #{name.inspect}")
  reservations = Reservation.where(
    user_id: @user.id,
    model_id: model.id,
    status: "unsubmitted"
  )
  expect(reservations.count).to be >= 1
  expect(reservations.map(&:pickup_location_id).uniq).to eq [nil]
end
