step "the global reservation timeout is :minutes minute(s)" do |minutes|
  Settings.first.update(timeout_minutes: minutes)
end

step "the following inventory exists:" do |table|
  table.hashes.each do |item|
    model = LeihsModel.find(product: item["model"]).presence \
      || FactoryBot.create(:leihs_model, product: item["model"])

    pool = InventoryPool.find(name: item["pool"]).presence \
      || FactoryBot.create(:inventory_pool, name: item["pool"])

    FactoryBot.create(
      :item,
      inventory_code: item["code"],
      leihs_model: model,
      owner: pool,
      responsible: pool
    )
  end
end

step "the cart is empty" do
  expect(page).to have_text "No items added"
end

step "I see these lines of text in the :title section:" do |title, table|
  expected_lines = [title].concat(table.to_a.flatten).join("\n")
  within(find_ui_section(title: title)) do
    expect(page).to have_text expected_lines
  end
end

step "I wait :seconds seconds" do |seconds|
  sleep(seconds.to_i)
end

step "I click on the :menu_item in the main navigation" do |menu_item|
  within(find(".ui-main-nav")) do
    case menu_item
    when "Leihs logo"
      click_on "Leihs"
    when "cart icon"
      find("a.ui-cart-item-link").click
    else
      fail "unknown menu item '#{menu_item}'"
    end
  end
end
