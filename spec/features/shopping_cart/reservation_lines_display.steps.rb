# FIXME: should use the UI (once its implemented)
step "I set my language to :lang" do |lang|
  expect(@user).to be_a User
  @user.language_locale = lang
  @user.save
end

step "the following reservations exist for the user:" do |table|
  table.hashes.each do |r|
    model = LeihsModel.find(product: r["model"]).presence || fail("Model not found: #{item["model"].inspect}")
    pool = InventoryPool.find(name: r["pool"]).presence || fail("Pool not found: #{item["pool"].inspect}")
    FactoryBot.create(
      :reservation,
      user: @user,
      quantity: r["quantity"].to_i,
      start_date: Date.parse(r["start-date"]),
      end_date: Date.parse(r["end-date"]),
      leihs_model: model,
      inventory_pool: pool,
    )
  end
end
