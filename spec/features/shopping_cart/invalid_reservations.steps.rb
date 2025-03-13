require_relative "invalid_reservations_data_setup"

step "a user with some mostly invalid reservations" do
  expect(create_all_sample_reservations).to be
end

step "I log in as the user" do
  log_in_as_user_with_email(user.email)
end

step "the :title dialog did not close" do |title|
  # Same as shared step "I see the :title dialog". Just so I can say "I click on the button, but the dialog did not close"
  dialog = find_ui_modal_dialog(title: title)
  expect(dialog).to be
end

step "I see the following warnings in the :title section:" do |section_name, table|
  section = find_ui_section(title: section_name)
  expect(section).to be
  within(section) do
    warnings = all(".invalid-feedback")
    expected_warnings = table.rows.flatten.map { |s|
      custom_interpolation(s, ->(o) { o.is_a?(Time) ? Locales.format_date(o, user) : o })
    }
    expect(warnings.map { |w| w.text }).to eq expected_warnings
  end
end

step "I enter :date_expr in the :field field" do |date_expr, field|
  date = custom_eval(date_expr)
  date_string = Locales.format_date(date, user)
  fill_in(field, with: date_string)
end

RSpec.configure do |config|
  config.include_context "invalid reservations data setup"
end
