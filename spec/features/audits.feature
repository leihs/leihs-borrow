Feature: Audits

  Background:
    Given there is an initial admin
    And there is a delegation "Delegation D"
    And there is a user "User A"
    And the user is member of delegation "Delegation D"
    And there is an inventory pool "Pool A"
    And the user is inventory manager of pool "Pool A"
    And there is an inventory pool "Pool B"
    And the user is inventory manager of pool "Pool B"
    And the delegation "Delegation D" is customer of pool "Pool A"
    And the delegation "Delegation D" is customer of pool "Pool B"

  Scenario: Audited requests
    Given there is a model "Kamera"
    And there is 1 borrowable item for model "Kamera" in pool "Pool A"
    And there are 2 borrowable items for model "Kamera" in pool "Pool B"
    And I log in as the user

    # +1 sign-in POST for entering username (1st step)
    # +1 sign-in POST for entering the password (2nd step)
    And there are 2 audited requests

    # search for a model
    And I visit "/borrow/"
    And I click on "Filter"
    And I enter "Kamera" in the search field
    And I select "Pool B" from the pools select box
    And I choose to filter by availabilty
    And I enter the date "${Date.tomorrow}" in the "From" field
    And I enter the date "${Date.tomorrow + 1.day}" in the "Until" field
    And I enter quantity "2"
    And I click button "Apply"
    Then I see one model with the title "Kamera"

    # make a reservation
    When I click on the model with the title "Kamera"
    Then the show page of the model "Kamera" was loaded
    And I click on "Add item"
    Then the order panel is shown
    And the pools select box shows "Pool B"
    And the start date has "${Date.tomorrow}"
    And the end date has "${Date.tomorrow + 1.day}"
    And the quantity has "2"
    And I enter quantity "1"
    And I click on "Add"
    And the "Add item" dialog has closed
    And I accept the "Item added" dialog with the text:
      """
      The item was added to the cart
      """
    And the "Item added" dialog has closed

    # +1
    And there are 3 audited requests

    # check the cart
    When I click on the cart icon
    Then I see the following lines in the "Items" section:
      | title     | body   |
      | 1× Kamera | Pool B |

    # submit the order
    When I click on "Send order"
    And I name the order as "Order 1"
    And I click on "Send"
    And the "Send order" dialog has closed
    And I accept the "Order submitted" dialog
    And the "Order submitted" dialog has closed

    # +1
    Then there are 4 audited requests
