step "I click on the user profile button" do
  find("nav .ui-user-profile-button").click
end

step "the user profile button shows :name" do |name|
  find("nav .ui-user-profile-button", text: name)
end
