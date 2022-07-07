# FIXME: should use the UI (once its implemented)
step "I set my language to :lang" do |lang|
  expect(@user).to be_a User
  @user.language_locale = lang
  @user.save
end
