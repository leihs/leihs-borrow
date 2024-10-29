step "the receival of received order emails is activated for all pools" do
  InventoryPool.all.each do |pool|
    pool.update(deliver_received_order_emails: true)
  end
end
