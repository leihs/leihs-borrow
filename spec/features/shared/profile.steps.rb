# Desktop (radix dropdown) and mobile (burger overlay) each render a
# .ui-user-profile-button; CSS shows only one per viewport. Wait for a
# *visible* one — do not probe desktop with wait:0 then fall through to
# mobile, or a still-loading desktop page waits on the hidden burger twin.
def find_visible_user_profile_button(**kwargs)
  find("nav .ui-user-profile-button", visible: :visible, match: :first, **kwargs)
end

step "I click on the user profile button" do
  find_visible_user_profile_button.click
end

step "the user profile button shows :name" do |name|
  expect(page).to have_css(
    "nav .ui-user-profile-button",
    text: name,
    visible: :visible
  )
end
