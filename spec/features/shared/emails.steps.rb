step "the receival of received order emails is activated" do
  Settings.first.update(deliver_received_order_notifications: true)
end
