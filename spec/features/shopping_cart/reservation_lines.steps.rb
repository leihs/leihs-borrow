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
