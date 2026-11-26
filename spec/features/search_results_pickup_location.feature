Feature: Search results - Filtering by pickup location

    The catalog's inventory pool filter lists the alternative pickup locations of each pool
    underneath it. Filtering by one of them means the item has to be transported there, so
    models that cannot be transported drop out of the results.

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And the inventory pool "Pool A" has the following details:
      | alternative pickup locations   | enabled |
      | transfer buffer before pick up | 0       |
    And the inventory pool "Pool A" has a pickup location "Media Lab"
    And the inventory pool "Pool A" has a pickup location "Studio Basement"
    And there is a model "DSLR Camera"
    And there is a model "Heavy Crane"
    And the model "Heavy Crane" is not transportable
    And the following items exist:
      | code | model       | pool   |
      | A11  | DSLR Camera | Pool A |
      | A21  | Heavy Crane | Pool A |

  Scenario: The pickup locations are listed under their pool

    When I log in as the user
    And I visit "/borrow/"
    Then the "Inventory pools" select offers these pools and pickup locations:
      | option              | indented |
      | All inventory pools | no       |
      | Pool A              | no       |
      | Media Lab           | yes      |
      | Studio Basement     | yes      |

  Scenario: Filtering by a pickup location hides models that cannot be transported

    Summary: Only what can be moved to the location is worth showing.

    When I log in as the user
    And I visit "/borrow/"
    And I select "Pool A" from "Inventory pools"
    Then I see 2 models
    And I see "DSLR Camera"
    And I see "Heavy Crane"

    When I select "Media Lab" from "Inventory pools"
    Then I see 1 model
    And I see "DSLR Camera"
    And I don't see "Heavy Crane"
    And the URL has the "pickup-location-id" query parameter for pickup location "Media Lab"

  Scenario: Clearing the pool filter clears the pickup location as well

    Summary: The pickup location is part of the pool filter, not a filter of its own, so it
    cannot survive its pool.

    When I log in as the user
    And I visit "/borrow/"
    And I select "Media Lab" from "Inventory pools"
    Then I see 1 model

    When I clear the inventory pools filter
    Then the URL has no "pickup-location-id" query parameter
    And the URL has no "pool-id" query parameter
    And I see 2 models

  Scenario: The filtered pickup location is preselected in the order dialog

    Summary: Having browsed the catalog for a location, the user should not have to pick it
    a second time.

    When I log in as the user
    And I visit "/borrow/"
    And I select "Media Lab" from "Inventory pools"
    And I click on the model with the title "DSLR Camera"
    And the show page of the model "DSLR Camera" was loaded
    And I click on "Add item"
    And I see the "DSLR Camera" dialog
    And I see the "Pickup location" section
    Then the "Pickup location" select shows "Media Lab"
