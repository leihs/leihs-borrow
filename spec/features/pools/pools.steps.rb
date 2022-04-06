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