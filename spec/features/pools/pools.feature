Feature: Pools

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And there is an inventory pool "Pool B"

    And there is a model "DSLR Camera"
    And the following items exist:
      | code | model       | pool   |
      | A1   | DSLR Camera | Pool A |

  Scenario: Pools index
    When the user is customer of pool "Pool B"
    And the user is customer of pool "Pool A"
    And I log in as the user
    And I visit "/borrow/inventory-pools"
    And I see the page title "Inventory Pools"

    Then I see the following lines in the "Available inventory pools" section:
      | title  | body                | foot |
      | Pool A |                     |      |
      | Pool B | No reservable items |      |

    When I click on the card with title "Pool A"
    Then I see the page title "Pool A"

    When I visit "/borrow/inventory-pools"
    And I see the page title "Inventory Pools"
    And I click on the card with title "Pool B"
    Then I see the page title "Pool B"
    And I see the text:
      """
      No reservable items
      """

  Scenario: Pool detail page
    Given the inventory pool "Pool A" has the following details:
      | contact                      | Call us at the front desk |
      | description                  | The best camera pool       |
      | maximum reservation duration | 14                         |
      | alternative pickup locations | enabled                   |
    And the inventory pool "Pool A" has a pickup location "Media Lab" with description "Ground floor, next to the workshop"
    And the inventory pool "Pool A" has a pickup location "Studio Basement" with description "Ring the bell"
    And the inventory pool "Pool A" is closed on "Sunday"
    And the inventory pool "Pool A" has a holiday "Christmas" from "2026-12-24" to "2026-12-26"
    And the user is customer of pool "Pool A"

    When I log in as the user
    And I visit "/borrow/inventory-pools"
    And I click on the card with title "Pool A"
    Then I see the page title "Pool A"

    And I see the "Contact" section
    And I see "Call us at the front desk"

    And I see the "Reservation constraint" section
    And I see "Maximum reservation duration 14 days"

    And I see the "Opening times" section
    And I see "Sunday"
    And I see "Closed"

    And I see the "Holidays" section
    And I see "Christmas"

    And I see the "Alternative pickup locations" section
    And I see "Media Lab"
    And I see "Ground floor, next to the workshop"
    And I see "Studio Basement"
    And I see "Ring the bell"

    And I see the "Description" section
    And I see "The best camera pool"

  Scenario: Pool detail page without alternative pickup locations

    Summary: The section is only there when the pool actually offers the feature.

    Given the inventory pool "Pool A" has a pickup location "Media Lab"
    And the user is customer of pool "Pool A"

    When I log in as the user
    And I visit "/borrow/inventory-pools"
    And I click on the card with title "Pool A"
    Then I see the page title "Pool A"
    And I don't see "Alternative pickup locations"
    And I don't see "Media Lab"

  Scenario: User has no pools
    When I log in as the user
    And I see the page title "Catalog"
    Then I see the text:
      """
      No reservable items found
      """

    When I click on "Check available inventory pools"
    Then I see the page title "Inventory Pools"
    And I see the text:
      """
      No inventory pool available
      """

  Scenario: User has no pools with reservable items
    When the user is customer of pool "Pool B"
    And I log in as the user
    And I see the page title "Catalog"
    Then I see the text:
      """
      No reservable items found
      """

    When I click on "Check available inventory pools"
    Then I see the page title "Inventory Pools"
    Then I see the following lines in the "Available inventory pools" section:
      | title  | body                | foot |
      | Pool B | No reservable items |      |
