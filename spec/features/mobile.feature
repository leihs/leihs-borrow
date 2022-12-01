Feature: Mobile screen

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And there is an inventory pool "Pool B"

    And there is a model "DSLR Camera"

    And I resize the window to mobile size

  Scenario: Using navigation menus
    When I log in as the user
    Then I see the page title "Catalog"

    # Open burger menu
    When I click on the menu
    Then I see "Borrow"
    And I see "Switch Section"

    # Close burger menu
    When I click on the menu
    Then I see the page title "Catalog"

    # Open burger menu and navigate to cart
    When I click on the menu
    And I click on "Cart"
    Then I see the page title "Cart"

    # Open profile menu
    When I click on the user profile button
    Then I see "User Account"
    And I see "Language"

    # Close profile menu
    When I click on the user profile button
    Then I see the page title "Cart"

    # Open profile menu and navigate to user account page
    When I click on the user profile button
    And I click on "User Account"
    Then I see the page title "User Account"
