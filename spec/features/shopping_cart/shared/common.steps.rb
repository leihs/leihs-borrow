step "I navigate to the cart" do
  visit "/borrow/order"
end

def create_reservations_from_table_for_user(user, table)
  expect(user).to be_a User
  table.hashes.each do |r|
    model = LeihsModel.find(product: r["model"]).presence || fail("Model not found: #{r["model"].inspect}")
    pool = InventoryPool.find(name: r["pool"]).presence || fail("Pool not found: #{r["pool"].inspect}")
    start_date = r["start-date"] ? Date.parse(r["start-date"]) : custom_eval(r["relative-start-date"]).to_date
    end_date = r["end-date"] ? Date.parse(r["end-date"]) : custom_eval(r["relative-end-date"]).to_date
    expect(start_date).to be_a Date
    expect(end_date).to be_a Date
    pickup_location = if r["pickup-location"].presence
      PickupLocation.find(name: r["pickup-location"], inventory_pool_id: pool.id).presence ||
        fail("Pickup location not found in pool #{pool.name.inspect}: #{r["pickup-location"].inspect}")
    end
    FactoryBot.create(
      :reservation,
      user: user,
      quantity: r["quantity"].to_i,
      start_date: start_date,
      end_date: end_date,
      leihs_model: model,
      inventory_pool: pool,
      pickup_location_id: pickup_location.try(:id),
      # Anything but "unsubmitted" makes the reservation block availability for
      # everybody else instead of sitting in this user's cart.
      status: r["status"].presence || "unsubmitted"
    )
  end
end

step "the following reservations exist for the user:" do |table|
  expect(@user).to be_a User
  create_reservations_from_table_for_user(@user, table)
end

step "the following reservations exist for the user :username:" do |username, table|
  user = User.find(login: username) || User.find(login: user_login_from_full_name(username))
  create_reservations_from_table_for_user(user, table)
end

step "I have been redirected to the orders list" do
  expect(current_path).to eq "/borrow/rentals/"
end

step "the newly created order in the DB has:" do |table|
  order = Order.order(Sequel.desc(:created_at)).first
  expect(order).to be
  expect(table.rows.length).to be 1
  table.hashes.first.each do |key, val|
    expect(order[key.to_sym]).to eq val
  end
end

step "I see a form inside the dialog" do
  expect(@dialog).to be
  @form = @dialog.find("form")
  expect(@form).to be
end

step "the form has an error message:" do |txt|
  expect(@form).to be
  within(@form) do
    err_msg = find(".invalid-feedback")
    scroll_to err_msg
    expect(err_msg.text).to eq txt
  end
end

step "the form has no error message" do
  expect(@form).to be
  within(@form) { expect(page).to have_no_selector ".invalid-feedback" }
end

step "the form has exactly these fields:" do |table|
  form_fields = within @form do
    all("section").map do |sec|
      sec.all("input,textarea,select", wait: 0).map do |field|
        field_id = field[:id]
        label = if field_id
          find("label[for='#{field_id}']", wait: 0)
        else
          field.find(:xpath, "./ancestor::label")
        end
        value = if field.tag_name === "select"
          field.find("option[value='#{field.value}']").text
        else
          field.value
        end
        {label: label.text, value: value}
      end
    end
  end.flatten

  # interpolate dates form values
  expected_fields = table.hashes.map { |h| h.merge({"value" => interpolate_dates_long(h["value"])}) }

  expect(form_fields).to eq(symbolize_hash_keys(expected_fields))
end

step "I see the following warnings in the :title section:" do |section_name, table|
  section = find_ui_section(title: section_name)
  expect(section).to be
  within(section) do
    warnings = all(".invalid-feedback")
    expected_warnings = table.rows.flatten.map { |s|
      custom_interpolation(s, ->(o) { o.is_a?(Time) ? Locales.format_date(o, @user) : o })
    }
    expect(warnings.map { |w| w.text }).to eq expected_warnings
  end
end

step "the :title dialog did not close" do |title|
  # Same as shared step "I see the :title dialog". Just so I can say
  # "I click on the button, but the dialog did not close"
  dialog = find_ui_modal_dialog(title: title)
  expect(dialog).to be
end
