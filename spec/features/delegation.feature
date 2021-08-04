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
    And I click on "Zeige Suche/Filter"
    And I enter "Kamera" in the search field
    And I select "Delegation D" xxx
    And I choose to filter by availabilty
    And I choose next working day as start date
    And I choose next next working day as end date
    And I click button "Get Results"
    Then I see one model with the title "Kamera"

    # make a reservation
    When I click on the model with the title "Kamera"
    Then the show page of the model "Kamera" was loaded
    And I click on "Gegenstand hinzufügen"
    Then the order panel is shown
    And I click on "Hinzufügen" and accept the alert

    # check the cart
    When I click on the menu
    And I click on "Cart"
    Then the cart page is loaded
    And I see one reservation for model "Kamera"
    And the reservation has quantity 1

    # submit the order
    When I name the order as "My order"
    And I click on "Confirm order"

    # approve the order in legacy
    When I visit the orders page of the pool "Pool A"
    Then I approve the order of the delegation

    # check the new status of the order
    When I visit "/app/borrow/"
    And I click on the menu
    And I click on "Orders"
    Then I see the order "My order" under approved orders

    # check the content of the order
    When I click on "My order"
    Then I see "1" times "Kamera"
