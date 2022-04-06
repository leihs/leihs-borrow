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
    And I visit "/app/borrow/inventory-pools"
    And I see the page title "Inventory Pools"

    Then I see the following lines in the "Available inventory pools" section:
      | title  | body                | foot |
      | Pool A |                     |      |
      | Pool B | No reservable items |      |

    When I click on "Pool A"
    Then I see the page title "Pool A"
    And I see the "E-mail" section

    When I visit "/app/borrow/inventory-pools"
    And I see the page title "Inventory Pools"
    And I click on "Pool B"
    Then I see the page title "Pool B"
    And I see the text:
      """
      No reservable items
      """
    And I see the "E-mail" section

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
