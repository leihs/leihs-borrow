Feature: Booking calendar pickup locations

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And the inventory pool "Pool A" has the following details:
      | alternative pickup locations | true |
    And there is a pickup location "Alt Site" for pool "Pool A"

  Scenario: Non-transportable model cannot select an alternative pickup location
    Given there is a model "Fixed Desk"
    And the model "Fixed Desk" is not transportable
    And there is 1 borrowable item for model "Fixed Desk" in pool "Pool A"

    When I log in as the user
    And I visit the model show page of model "Fixed Desk"
    Then the show page of the model "Fixed Desk" was loaded

    When I click on "Add item"
    Then the order panel is shown
    And the calendar has finished loading
    And I see "Pickup and return only possible at the main location of the inventory pool."
    And the pickup location select is not shown

    When I click on "Add"
    And the "Add item" dialog has closed
    And I accept the "Item added" dialog with the text:
      """
      The item was added to the cart
      """
    And the "Item added" dialog has closed

    When I click on the cart icon
    Then I see the following lines in the "Items" section:
      | title         | body                                                                   |
      | 1× Fixed Desk | Pool A\n${format_date_range_short(Date.today, Date.tomorrow)} (2 days) |
    And the unsubmitted reservation for model "Fixed Desk" has no pickup location

  Scenario: Transportable model can select an alternative pickup location
    The calendar shows the pickup-location select only when the pool has at
    least one activated pickup location.

    Given there is a model "Camera"
    And there is 1 borrowable item for model "Camera" in pool "Pool A"

    When I log in as the user
    And I visit the model show page of model "Camera"
    Then the show page of the model "Camera" was loaded

    When I click on "Add item"
    Then the order panel is shown
    And the calendar has finished loading
    And the pickup location select is shown
    And I see "Alt Site"

  Scenario: Pickup location select is hidden when no location is activated
    Enabling alternative pickup locations is not enough. The calendar hides
    the select when the pool has no activated pickup location.

    Given there is a model "Camera"
    And there is 1 borrowable item for model "Camera" in pool "Pool A"
    And the pickup location "Alt Site" for pool "Pool A" is deactivated

    When I log in as the user
    And I visit the model show page of model "Camera"
    Then the show page of the model "Camera" was loaded

    When I click on "Add item"
    Then the order panel is shown
    And the calendar has finished loading
    And the pickup location select is not shown
    And I don't see "Alt Site"
