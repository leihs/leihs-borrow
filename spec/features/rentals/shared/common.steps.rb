step "a customer order with title :title and the following reservations exists for the user:" do |title, table|
  table.hashes.each do |h|
    database.transaction do
      u = case h["user"]
      when "user", nil then @user
      else
        User.find(firstname: h["user"])
      end
      p = InventoryPool.find(name: h["pool"])
      m = LeihsModel.find(product: h["model"])
      opt = Option.find(product: h["option"])
      o = Order.find(title: title)
      o ||= FactoryBot.create(:order, title: title, user_id: u.id)
      po = PoolOrder.find(inventory_pool_id: p.id, customer_order_id: o.id)
      po ||= FactoryBot.create(:pool_order,
        inventory_pool_id: p.id,
        user_id: u.id,
        state: if ["submitted", "approved", "rejected", "canceled"].include?(h["state"])
                 h["state"]
               else
                 "approved"
               end,
        customer_order_id: o.id)
      c = if ["signed", "closed"].include?(h["state"])
        Contract.create_with_disabled_triggers(UUIDTools::UUID.random_create.to_s,
          u.id,
          p.id,
          case h["state"]
          when "signed" then :open
          when "closed" then :closed
          end)
      end
      if h["pickup-date"].presence
        db_with_disabled_triggers do
          Contract.update_with_disabled_triggers(c.id, :created_at, "'#{h["pickup-date"]}'")
        end
      end
      h["quantity"].to_i.times do
        FactoryBot.create(:reservation,
          inventory_pool_id: p.id,
          user_id: u.id,
          status: h["state"],
          start_date: h["start-date"] ? Date.parse(h["start-date"]) : custom_eval(h["relative-start-date"]).to_date,
          end_date: h["end-date"] ? Date.parse(h["end-date"]) : custom_eval(h["relative-end-date"]).to_date,
          model_id: m.try(:id),
          option_id: opt.try(:id),
          order_id: po.id,
          contract_id: c.try(:id))
      end
    end
  end
end

# Override for the equally named step, but with date interpolation
step "the page subtitle is :subtitle" do |subtitle|
  subtitle = interpolate_dates_short(subtitle)
  expect(@page).to be
  expect(@page[:subtitle]).to eq subtitle
end

step "I see the following status rows in the :name section:" do |section_name, table|
  section = find_ui_section(title: section_name)
  status_rows = get_ui_progress_infos(section)
  # ignore keys that are not present in the expectations table by removing them:
  actual_status_rows = status_rows.map { |l| l.slice(*table.headers.map(&:to_sym)) }
  expect(actual_status_rows).to eq symbolize_hash_keys(table.hashes)
end

step "the :name button is not visible" do |name|
  page.has_no_selector?("button", text: name)
end
