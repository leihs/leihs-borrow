step "I see the page title :title" do |title|
  wait_until {
    @page = get_ui_page_headings
    @page[:title] === title
  }
end

step "the page subtitle is :subtitle" do |subtitle|
  expect(@page).to be
  expect(@page[:subtitle]).to eq subtitle
end

step "the calendar has finished loading" do
  wait_until { page.has_no_selector? ".rdrMonthPassiveOverlay" }
end

step "I see the :name section" do |section_name|
  @section = find_ui_section(title: section_name)
  expect(@section).to be
end

step "I see the following text in the :name section:" do |section_name, txt|
  section = find_ui_section(title: section_name)
  expect(section).to be
  within(section) do
    section_text = find("div")
    expect(section_text.text).to eq txt
  end
end

step "I see the :title dialog" do |title|
  @dialog = find_ui_modal_dialog(title: title)
  expect(@dialog).to be
end

step "the :title dialog has closed" do |title|
  expect(find_ui_modal_dialog(title: title, present: false)).to be
end

step "I accept the :title dialog" do |title|
  within(find_ui_modal_dialog(title: title)) do
    find("button.btn-primary").click
  end
end

step "I accept the :title dialog with the text:" do |title, text|
  within(find_ui_modal_dialog(title: title)) do
    expect(find(".modal-body").text).to eq interpolate_dates_short(text)
    find("button.btn-primary").click
  end
end

def expect_equal_card_data(card_data, table) 
  # ignore keys that are not present in the expectations table by removing them:
  actual_data = card_data.map { |l| l.slice(*table.headers.map(&:to_sym)) }

  # interpolate dates in expected foot
  expected_lines = table.hashes.map { |h| h["foot"].nil? ? h : h.merge({ "foot" => interpolate_dates_short(h["foot"]) }) }

  expect(actual_data).to eq symbolize_hash_keys(expected_lines)
end

step "I see the following lines in the :name section:" do |section_name, table|
  items_section = find_ui_section(title: section_name)
  card_data = get_ui_list_cards(items_section)
  expect_equal_card_data(card_data, table)
end

step "I see the following lines in the page content:" do |table|
  content = find_ui_page_content
  card_data = get_ui_list_cards(content)
  expect_equal_card_data(card_data, table)
end

step "I click on the card with title :title" do |title|
  card = get_ui_list_card_by_title(title)
  expect(card).to be
  card.click
end
