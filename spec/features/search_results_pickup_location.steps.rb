step "the :label select offers these pools and pickup locations:" do |label, table|
  # Pickup locations are indented under their pool with non-breaking spaces, which
  # the browser reports back as ordinary ones.
  options = select_for_label(label).all("option").map do |option|
    text = option.text
    stripped = text.sub(/\A[[:space:] ]+/, "")
    {"option" => stripped, "indented" => (stripped == text) ? "no" : "yes"}
  end
  expect(options).to eq table.hashes
end

step "I see :n model(s)" do |n|
  if n.to_i.zero?
    expect(page).to have_no_css ".ui-models-list-item"
  else
    expect(page).to have_css ".ui-models-list-item", count: n.to_i
  end
end

step "I clear the inventory pools filter" do
  find("button[aria-label='Clear filter']").click
end

step "the URL has no :param query parameter" do |param|
  query = URI.parse(current_url).query.to_s
  expect(Rack::Utils.parse_nested_query(query)).not_to have_key param
end

step "the URL has the :param query parameter for pickup location :location_name" do |param, location_name|
  location = PickupLocation.find(name: location_name) || fail("Pickup location not found: #{location_name.inspect}")
  query = URI.parse(current_url).query.to_s
  expect(Rack::Utils.parse_nested_query(query)[param]).to eq location.id
end
