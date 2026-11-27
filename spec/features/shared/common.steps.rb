step "I pry" do
  binding.pry # standard:disable Lint/Debugger
end

step "I debug :code" do |code|
  eval(code) # standard:disable Security/Eval
end

step "there is an empty database" do
  reset_database
end

step "I select :option from :label" do |option, label|
  if label == "Switch Profile"
    # Profile switch eventually calls window.location.reload (after GraphQL + 50ms).
    # Waiting only for a stale profile button is too early: re-frame
    # re-renders (changing-to spinner) remount the button before reload. Mark the
    # document and wait until the marker disappears after a full reload.
    #
    # Selecting the already-active option does not fire onChange (no reload) — e.g.
    # "User A (personal)" right after login — so skip the marker wait in that case.
    field = find_field(label)
    already_selected = field.find(:option, option).selected?
    unless already_selected
      page.execute_script("window.__leihsProfileSwitchMarker = true")
      select(option, from: label)
      wait_until do
        page.evaluate_script("window.__leihsProfileSwitchMarker") != true
      end
    end
    expect(page).to have_css("nav .ui-user-profile-button", visible: :visible)
  else
    select(option, from: label)
  end
end

step "I click on :txt" do |txt|
  click_on txt
end

step "I click on model :name" do |name|
  find(".ui-models-list-item", text: name).click
end

step "I check :txt" do |txt|
  check txt
end

step "I click on :txt and accept the alert" do |txt|
  accept_alert { click_on txt }
end

step "I enter :value in the :name field" do |value, name|
  fill_in name, with: value
end

step "I enter the date :date_expr in the :name field" do |date_expr, name|
  date = custom_eval(date_expr)
  date_string = Locales.format_date(date, @user)
  fill_in(name, with: "")
  fill_in(name, with: date_string)
end

step "I see the date :date_expr in the :name field" do |date_expr, name|
  date = custom_eval(date_expr)
  date_string = Locales.format_date(date, @user)
  find_field(name, with: date_string)
end

step "I go to :url" do |url|
  visit url
end

step "I visit :url" do |url|
  visit url
end

step "I am on :path" do |path|
  expect(page.current_path).to eq path
end

step "I am redirected to :url" do |url|
  binding.pry if url == "?" # standard:disable Lint/Debugger
  wait_until(10) { expect(page.current_path).to eq url }
end

step "I see the text:" do |txt|
  expect(page).to have_content(txt.strip)
end

step "I see :txt" do |txt|
  expect(page).to have_content txt
end

step "I don't see :txt" do |txt|
  expect(page).not_to have_content(txt.to_s.strip)
end

step "I log in with the email :email" do |email|
  @current_user = log_in_as_user_with_email(email)
end

step "I log in as the user" do
  expect(@user).to be_a User
  log_in_as_user_with_email(@user.email)
end

step "I log in as the user :full_name" do |name|
  find_user_by_full_name!(name)
  expect(@user).to be_a User
  log_in_as_user_with_email(@user.email)
end

step "user's preferred language is :lang" do |lang|
  l = Language.find(name: lang)
  @user.update(language_locale: l.locale)
end

step "user does not have a prefered language" do
  expect(@user.reload.language_locale).to be_nil
end

step "(I )sleep :n" do |n|
  sleep n.to_f
end

step "I wait for :n second(s)" do |n|
  step "sleep #{n}"
end

step "I eval :code" do |code|
  eval(code) # standard:disable Security/Eval
end

step "I click button :name" do |name|
  click_button(name)
end

step "there is an error message" do
  page.has_content?(/error/i)
end

step "I log in as the initial admin" do
  log_in_as_user_with_email(@initial_admin.email)
end

step "I log in as the leihs admin" do
  log_in_as_user_with_email(@leihs_admin.email)
end

step "I fill out the form with:" do |table|
  fill_form_with_table(table)
end

step "the title of the page is :title" do |title|
  find("h1", text: title)
end

def fill_form_with_table(table)
  table.hashes.each do |row|
    fill_in(row["field"], with: row["value"])
  end
end

step "I click on the cart icon" do
  find("a.ui-cart-item-link").click
end

step "I reload the page" do
  visit current_path
end

step "the :title button is disabled" do |title|
  expect(find("button", text: title)).to be_disabled
end

step "the :title button is not disabled" do |title|
  expect(find("button", text: title)).not_to be_disabled
end

step "I press the tab key" do
  send_keys :tab
end
