step "there is an inventory pool :name with date restrictions" do |name|
  @pool = FactoryBot.create(:inventory_pool, name: name)
  @pool.update(
    borrow_reservation_advance_days: 2,
    borrow_maximum_reservation_duration: 7
  )

  FactoryBot.create(:holiday,
    start_date: 15.days.from_now,
    end_date: 16.days.from_now,
    inventory_pool: @pool,
    name: "Yolo")

  # current weekday is non-working-day, and weekday before has max visits reached
  cwday_today = Date.today.cwday
  wday_yesterday = Date.yesterday.wday
  Workday.find(inventory_pool_id: @pool.id)
    .update(monday: cwday_today != 1,
      tuesday: cwday_today != 2,
      wednesday: cwday_today != 3,
      thursday: cwday_today != 4,
      friday: cwday_today != 5,
      saturday: cwday_today != 6,
      sunday: cwday_today != 7,
      max_visits: {"1": (wday_yesterday == 1) ? "0" : "1",
                   "2": (wday_yesterday == 2) ? "0" : "1",
                   "3": (wday_yesterday == 3) ? "0" : "1",
                   "4": (wday_yesterday == 4) ? "0" : "1",
                   "5": (wday_yesterday == 5) ? "0" : "1",
                   "6": (wday_yesterday == 6) ? "0" : "1",
                   "0": (wday_yesterday == 0) ? "0" : "1"})
end

step "the user is suspended in :pool" do |pool|
  pool = InventoryPool.find(name: pool)
  expect(@user).to be_a User
  FactoryBot.create(:suspension, user: @user, inventory_pool: pool)
end

step "I see the following categories:" do |table|
  cat_list = find(".ui-category-list")
  cat_items = cat_list.all(".ui-square-image-grid-item")
  captions = cat_items.map { |item| item.find(".ui-caption", wait: false).text }

  expected_captions = table.hashes.map { |h| h["caption"] }
  expect(captions).to eq expected_captions
end

def get_category_item_by_caption(caption)
  cat_list = find(".ui-category-list")
  cat_list.all(".ui-square-image-grid-item").find do |item|
    c = item.find(".ui-caption", wait: false).text
    c == caption
  end
end

step "I click on the item captioned :caption in the category list" do |caption|
  cat_item = get_category_item_by_caption(caption)
  expect(cat_item).to be
  cat_item.click
end

step "I see the following models:" do |table|
  models_list = find(".ui-models-list")
  model_items = models_list.all(".ui-square-image-grid-item")
  captions = model_items.map { |item| item.find(".ui-caption", wait: false).text }

  expected_captions = table.hashes.map { |h| h["caption"] }
  expect(captions).to eq expected_captions
end

step "I see the following text in the dialog:" do |txt|
  expect(@dialog).to be
  expect(@dialog).to have_content(txt.strip)
end

step "I see the :title dialog with the text:" do |title, text|
  within(find_ui_modal_dialog(title: title)) do
    expect(find(".modal-body").text).to eq text
  end
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

step "I press the tab key" do
  send_keys :tab
end

step "the submit button is disabled" do
  expect(find_button("Add", disabled: true)).to be
end
