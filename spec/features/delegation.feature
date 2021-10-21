Feature: Delegations

  Background:
    Given there is an initial admin
    And there is a delegation "Delegation D"
    And there is a user
    And the user is member of delegation "Delegation D"
    And there is an inventory pool "Pool A"
    And the user is inventory manager of pool "Pool A"
    And the delegation "Delegation D" is customer of pool "Pool A"

  Scenario: Order for delegation
    Given there is a model "Kamera"
    And there is 1 borrowable item for model "Kamera" in pool "Pool A"
    When I log in as the user

    # search for a model
    And I visit "/app/borrow/"
    And I click on "Show search/filter"
    And I enter "Kamera" in the search field
    And I select "Delegation D" xxx
    And I choose to filter by availabilty
    And I choose next working day as start date
    And I choose next next working day as end date
    And I click button "Apply"
    Then I see one model with the title "Kamera"

    # make a reservation
    When I click on the model with the title "Kamera"
    Then the show page of the model "Kamera" was loaded
    And I click on "Add item"
    Then the order panel is shown
    And I click on "Add"
    And the "Add item" dialog has closed
    And I accept the "Item added" dialog with the text:
      """
      The item was added to the new rental
      """
    And the "Item added" dialog has closed

    # check the cart
    When I click on the menu
    And I click on "New Rental"
    Then I see the following lines in the "Items" section:
      | title     | body   |
      | 1× Kamera | Pool A |

    # check that the delegations select field is disabled in the order panel if cart is not empty
    When I visit the show page for "Kamera" model
    And I click on "Add item"
    # Then the delegations select field is disabled
    # And there is an error message below the field
    And I click on "Cancel"

    # submit the order
    When I click on the menu
    And I click on "New Rental"
    And I click on "Confirm rental"
    And I name the order as "My order"
    And I click on "Confirm"
    And the "Confirm new rental" dialog has closed
    And I accept the "Order submitted" dialog
    And the "Order submitted" dialog has closed

    # approve the order in legacy
    When I visit the orders page of the pool "Pool A"
    Then I approve the order of the delegation

    # check the new status of the order
    When I visit "/app/borrow/"
    # FIXME: wait for menu open
    And I sleep 1
    And I click on the menu
    And I click on "My Rentals"
    And I click on "Show search/filter"
    And I select "Delegation D" xxx
    And I click button "Apply"
    Then I see the order "My order" under open orders

    # check the content of the order
    When I click on "My order"
    Then I see "1× Kamera"
