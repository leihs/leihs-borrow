step "I enter :term in the search field" do |term|
  fill_in("Search", with: term)
end

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

step "the start date chosen on the previous screen is pre-filled" do
  expect(find("input[name='start-date']").value).to eq Date.today.to_s
end

step "the end date chosen on the previous screen is pre-filled" do
  expect(find("input[name='end-date']").value).to eq Date.tomorrow.to_s
end

step "I click on :text and accept the alert" do |text|
  accept_alert { click_on(text) }
end

step "the cart page is loaded" do
  expect(page).to have_content ["New rental", "Status"].join("\n")
end

step "the reservation has quantity :n" do |n|
  within find(".flex-row") do
    expect(current_scope).to have_content "#{n} Item(s)"
  end
end

step "I visit the model show page of model :name" do |name|
  m = LeihsModel.find(product: name)
  visit "/app/borrow/models/#{m.id}"
end

step ":term is pre-filled as the search term" do |term|
  expect(find("input[name='search-term']").value).to eq term
end

step "I delete the reservation for model :name" do |name|
  find(".flex-row", text: name).find(".ui-trash-icon").click
end

step "the reservation for model :name was deleted from the cart" do |name|
  expect(page).not_to have_selector(".flex-row", text: name)
end

step "I name the order as :title" do |title|
  fill_in("title", with: title)
end

step "the cart is empty" do
  expect(page).to have_content "Your order is empty"
end

step "I visit the orders page of the pool :name" do |name|
  pool = InventoryPool.find(name: "Pool A")
  visit("http://localhost:#{LEIHS_LEGACY_HTTP_PORT}/manage/#{pool.id}/orders")
end

step "I approve the order of the user/delegation" do
  find("[data-order-approve]").click
end

step "I see the order :purpose under open orders" do |purpose|
  within find("section", text: "Open") do
    expect(current_scope).to have_content purpose
  end
end

step "the maximum quantity shows :n" do |n|
  expect(page).to have_content /#{n}.max/
end

step "the search filters are persisted in the url" do
  p_hash = Rack::Utils.parse_nested_query(URI.parse(current_url).query)
  expect(p_hash).to eq(
    {
      "only-available" => "true",
      "quantity" => "1",
      "start-date" => Date.today.to_s,
      "end-date" => Date.tomorrow.to_s,
      "term" => "Kamera",
      "user-id" => @user.id,
    }
  )
end

step "I clear ls from the borrow app-db" do
  find(".ui-menu-icon").click
  click_on("Clear :ls")
end

step "I visit the url with query params for dates as before but :m_name as term" do |m_name|
  visit "/app/borrow/" \
        "?only-available=true" \
        "&start-date=#{Date.today}" \
        "&end-date=#{Date.tomorrow}" \
        "&term=#{m_name}"
end

step "I click on :label for the model :name" do |label, name|
  find(".flex-row", text: name).click_button("Edit")
end

step "I increase the start date by 1 day for the model :name" do |name|
  fill_in("from", with: Date.tomorrow.to_s)
end

step "I increase the end date by 1 day for the model :name" do |name|
  fill_in("until", with: (Date.tomorrow + 1.day).to_s)
end

step "the reservation data was updated successfully for model :name" do |name|
  within find(".flex-1", text: "Kamera") do
    s = Date.tomorrow.strftime("%-m/%-d/%Y")
    e = (Date.tomorrow + 1.day).strftime("%-m/%-d/%Y")
    expect(current_scope).to have_content(/#{s}...#{e}/)
    expect(current_scope).to have_content("4 Items")
  end
end

step "I see :n times :name" do |n, name|
  pre = find("pre", match: :first).text
  o = JSON.parse(pre).deep_symbolize_keys
  rs = o[:"sub-orders-by-pool"].first[:reservations]
  expect(rs.count).to eq n.to_i
  m = LeihsModel.find(product: name)
  expect(rs.map { |r| r[:model][:id] }.uniq).to eq [m.id]
end

step "I select :name xxx" do |name|
  all(
    # WTF: this should work but throws `Selenium::WebDriver::Error::JavascriptError: TypeError: cyclic object value`
    # find('select[name="user-id"] option', text: name).select_option
    'select[name="user-id"] option'
  )
    .select { |n| n.text.include?(name) }.first
    .select_option
end

step "I click on the menu" do
  find("nav .ui-menu-icon").click
end

step "the order panel is shown" do
  find(".ui-booking-calendar")
end

step "I visit the show page for :name model" do |name|
  m = LeihsModel.find(product: name)
  visit "/app/borrow/models/#{m.id}"
end

step "the delegations select field is disabled" do
  binding.pry
end

step "there is an error message below the field" do
  binding.pry
end

step "I accept the :title dialog with the text:" do |title, text|
  within(find_ui_modal_dialog(title: title)) do
    expect(find(".modal-body").text).to eq text
    click_on "OK"
  end
end

step "I accept the :title dialog" do |title|
  within(find_ui_modal_dialog(title: title)) do
    click_on "OK"
  end
end
