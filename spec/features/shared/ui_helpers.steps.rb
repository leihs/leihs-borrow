step "I see the page title :title" do |title|
  wait_until { get_ui_page_layout[:title] === title }
end

step "the calendar has finished loading" do
  wait_until { page.has_no_selector? ".rdrMonthPassiveOverlay" }
end

step "I see the :name section" do |section_name|
  @section = find_ui_section(title: section_name)
  expect(@section).to be
end

step "I see the :title dialog" do |title|
  @dialog = find_ui_modal_dialog(title: title)
  expect(@dialog).to be
end

step "the :title dialog has closed" do |title|
  expect(find_ui_modal_dialog(title: title, present: false)).to be
end

step "I see the following lines in the :name section:" do |section_name, table|
  items_section = find_ui_section(title: section_name)
  item_lines = get_ui_list_cards(items_section)
  # ignore keys that are not present in the expectations table by removing them:
  expected_item_lines = item_lines.map { |l| l.slice(*table.headers.map(&:to_sym)) }
  expect(expected_item_lines).to eq symbolize_hash_keys(table.hashes)
end
