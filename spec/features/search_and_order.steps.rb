step "I enter :term in the search field" do |term|
  fill_in("Search", with: term)
end

step "I select :name from the pools select box" do |name|
  select(name, from: "Inventory pools")
end

step "I enter quantity :q" do |q|
  find("#quantity").set q
end

step "the quantity has :q" do |q|
  expect(find("#quantity").value).to eq q
end

step "the start date has :ruby_code" do |ruby_code|
  d = custom_eval(ruby_code)
  date_string = Locales.format_date(d, @user)
  expect(find("#startDate").value).to eq date_string
end

step "the end date has :ruby_code" do |ruby_code|
  d = custom_eval(ruby_code)
  date_string = Locales.format_date(d, @user)
  expect(find("#endDate").value).to eq date_string
end

step "I set :ruby_code as the start date" do |ruby_code|
  d = custom_eval(ruby_code)
  date_string = Locales.format_date(d, @user)
  find("#startDate").set date_string
end

step "I set :ruby_code as the end date" do |ruby_code|
  d = custom_eval(ruby_code)
  date_string = Locales.format_date(d, @user)
  find("#endDate").set date_string
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
  expect(page).to have_content ["Cart", "Status"].join("\n")
end

step "the reservation has quantity :n" do |n|
  within find(".flex-row") do
    expect(current_scope).to have_content "#{n} Item(s)"
  end
end

step "I visit the model show page of model :name" do |name|
  m = LeihsModel.find(product: name)
  visit "/borrow/models/#{m.id}"
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

step "the cart is empty" do
  expect(page).to have_content "Your order is empty"
end

step "I approve the order :title" do |title|
  database.transaction do
    c_order = Order.find(title: title)
    PoolOrder.where(customer_order_id: c_order.id).each do |pool_order|
      pool_order.update(state: "approved")
      Reservation.where(order_id: pool_order.id).update(status: "approved")
    end
  end
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
  visit "/borrow/" \
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

step "the order panel is shown" do
  find(".ui-booking-calendar")
end

step "I visit the show page for :name model" do |name|
  m = LeihsModel.find(product: name)
  visit "/borrow/models/#{m.id}"
end

step "the delegations select field is disabled" do
  binding.pry
end

step "there is an error message below the field" do
  binding.pry
end

step "there are no audited requests" do
  expect(AuditedRequest.count).to eq 0
end

step "there is/are :n audited request(s)" do |n|
  expect(AuditedRequest.count).to eq n.to_i
end

step "a submitted email has been created for user :full_name" do |full_name|
  first, last = full_name.split
  u = User.find(firstname: first, lastname: last)
  e = Email.find(to_address: u.email,
                 user_id: u.id,
                 subject: "[leihs] Reservation Submitted")
end

step "a received email has been created for pool :name" do |name|
  ip = InventoryPool.find(name: name)
  e = Email.find(to_address: ip.email,
                 inventory_pool_id: ip.id,
                 subject: "[leihs] Order Received")
end

step "there have been :n emails created" do |n|
  expect(Email.count).to eq n.to_i
end

step "the cart timeout is set to :n minute" do |n|
  Settings.first.update(timeout_minutes: n.to_i)
end

step "the cart is expired" do
  expect(find(".ui-progress-info", text: "Time limit")).to have_content "Expired"
end

step "I see the following lines in the Items section:" do |table|
  rs = find("section", text: "Items").all(".list-card")
  table.hashes.each_with_index do |h, i|
    expect(rs[i].find("[data-test-id='title']").text).to eq h["title"]
    expect(rs[i].find("[data-test-id='body']").text).to eq h["pool"]
    b = rs[i].find("[data-test-id='foot'] .badge#{h['valid'] == 'false' and '.bg-danger'}")
    days = ( h['duration'].to_i > 1 ? "days" : "day" )
    date_string = custom_eval(h['start_date']).strftime('%d/%m/%y')
    expect(b.text).to eq "#{h['duration']} #{days} from #{date_string}"
  end
end

