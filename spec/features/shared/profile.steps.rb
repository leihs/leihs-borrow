step "I click on the profile button" do
  find("nav .ui-profile-button").click
end

step "the profile button shows :name" do |name|
  find("nav .ui-profile-button", text: name)
end