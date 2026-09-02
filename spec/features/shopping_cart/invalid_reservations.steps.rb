require_relative "invalid_reservations_data_setup"

step "a user with some mostly invalid reservations" do
  expect(create_all_sample_reservations).to be
end

step "a user with invalid alternative pickup reservations" do
  expect(user).to be
  expect(inventory_pool_alt_pickup).to be
  expect(pickup_location_alt).to be
  expect(r7_non_transportable_alt).to be
  expect(r8_transfer_buffer_before_pickup).to be
  expect(r9_gone_pickup_location).to be
end

step "the calendar has finished loading" do
  scope = @dialog || page
  within(scope) do
    expect(page).to have_css("#order-dialog-form", wait: 15)
    if page.has_css?(".opcal", wait: 15)
      next
    end
    find_ui_section(title: "Time span")
  end
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
  scope = @dialog || page
  within(scope) do
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
end

step "I enter :date_expr in the :field field" do |date_expr, field|
  date = custom_eval(date_expr)
  date_string = Locales.format_date(date, user)
  fill_in(field, with: date_string)
end

RSpec.configure do |config|
  config.include_context "invalid reservations data setup"
end
