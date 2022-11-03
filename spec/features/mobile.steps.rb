step "I resize the window to mobile size" do
  page.driver.browser.manage.window.resize_to(450, 600)
end

step "I click on the menu" do
  find("nav .ui-menu-icon").click
end