Feature: Access rights

  Background:
    Given there is an initial admin
    And there is a user "User A"
    And there is an inventory pool "Pool A"
    And the user "User A" is customer of pool "Pool A"
    And the user has suspension for pool "Pool A" which is expired
    And there is a model "Kamera"
    And there is 1 borrowable item for model "Kamera" in pool "Pool A"
    And I log in as the user

  Scenario: Expired suspension should not have any effect on the customer
    When I visit "/borrow/"
    And I enter "Kamera" in the search field
    And I click on "Search"
    Then I see one model with the title "Kamera"

    When I click on the model with the title "Kamera"
    Then the show page of the model "Kamera" was loaded
    And I click on "Add item"
    Then the order panel is shown
    And the pools select box shows "Pool A"
    And I click on "Add"
    And the "Add item" dialog has closed
    And I accept the "Item added" dialog with the text:
    """
    The item was added to the cart
    """
    And the "Item added" dialog has closed

    When I click on the cart icon
    And I click on "Send order"
    And I name the order as "Order 1"
    And I click on "Send"
    And the "Send order" dialog has closed
    And I accept the "Order submitted" dialog
    And the "Order submitted" dialog has closed
