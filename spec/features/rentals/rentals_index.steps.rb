step "I see the following rentals:" do |table|
  table.hashes.each do |h|
    find(".ui-list-card", text: h["title"])
  end
  expect(all(".ui-list-card").count).to eq table.hashes.count
end

step "I enter :value in the :field field" do |value, field|
  value2 = case value
    when "day after tomorrow"
      Locales.format_date(Date.tomorrow + 1.day, @user)
    when "yesterday"
      Locales.format_date(Date.yesterday, @user)
    else
      value
    end
  el = find_field(field)
  simulate_typing(el, value2)
end

step "the :label select field contains value :label" do |label, value|
  expect(page).to have_select(label, selected: value)
end

step "the :label input field has value :value" do |label, value|
  expect(find("section", text: label).find("input").value).to eq value
end
