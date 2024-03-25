step "the user has suspension for pool :name which is expired" do |name|
  ip = InventoryPool.find(name: name)
  FactoryBot.create(:suspension, inventory_pool: ip, suspended_until: Date.yesterday)
end

step "I enter :name in the search field" do |name|
  fill_in("term", with: name)
end
