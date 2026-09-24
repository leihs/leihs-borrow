step "I see the following text in the dialog:" do |txt|
  expect(@dialog).to be
  expect(@dialog).to have_content(txt.strip)
end

step "I see the :title dialog with the text:" do |title, text|
  within(find_ui_modal_dialog(title: title)) do
    expect(find(".modal-body").text).to eq text
  end
end

step "I see a warning in the dialog:" do |txt|
  expect(@dialog).to be
  within(@dialog) do
    err_msg = find(".invalid-feedback")
    scroll_to err_msg
    expect(err_msg.text).to eq txt
  end
end

step "the :title button is disabled" do |title|
  expect(find("button", text: title)).to be_disabled
end

step "I press the tab key" do
  send_keys :tab
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

step "I see no warnings in the :title section" do |section_name|
  section = find_ui_section(title: section_name)
  expect(section).to be
  within(section) do
    expect(page).to have_no_css(".invalid-feedback")
  end
end
