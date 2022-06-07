Feature: Request framework

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And there is a borrowable item in pool "Pool A"

  Scenario: Login and retry after having been logged out
    Given I log in as the user
    And I visit the model show page for the borrowable item
    And I see "Add item"

    When I clear the browser cookies
    And I click on "Add item"
    And the order panel is shown
    And I click on "Add"
    Then I see "Error"
    And I see "User not logged in"

    # 1st Retry (still logged out)
    When I click on "OK"
    And I click on "Add"
    Then I see "Error"

    # Login
    When I click on "Go to login"
    And I log in again
    Then I see "Add item"

    # 2nd Retry (now logged in)
    When I click on "Add item"
    And I click on "Add"
    And I accept the "Item added" dialog
    Then the "Item added" dialog has closed
    And the cart is not empty
