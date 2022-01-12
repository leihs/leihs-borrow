step "I navigate to the cart" do
  visit "/app/borrow/order"
end

step "the following items exist:" do |table|
  table.hashes.each do |item|
    model = LeihsModel.find(product: item["model"]).presence || fail("Model not found: #{item["model"].inspect}")
    pool = InventoryPool.find(name: item["pool"]).presence || fail("Pool not found: #{item["pool"].inspect}")
    FactoryBot.create(
      :item,
      inventory_code: item["code"],
      leihs_model: model,
      owner: pool,
      responsible: pool,
    )
  end
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
    FactoryBot.create(
      :reservation,
      user: user,
      quantity: r["quantity"].to_i,
      start_date: start_date,
      end_date: end_date,
      leihs_model: model,
      inventory_pool: pool,
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

step "I have been redirected to the newly created order" do
  wait_until { get_ui_page_layout }
  # FIXME: we should not have to wait here!
  # @order = wait_until { Order.order(Sequel.desc(:created_at)).first }
  @order = Order.order(Sequel.desc(:created_at)).first
  current_path == "/app/borrow/rentals/#{@order.id}"
  expect(current_path).to eq "/app/borrow/rentals/#{@order.id}"
end

step "the newly created order in the DB has:" do |table|
  expect(@order).to be
  expect(table.rows.length).to be 1
  table.hashes.first.each do |key, val|
    expect(@order[key.to_sym]).to eq val
  end
end
