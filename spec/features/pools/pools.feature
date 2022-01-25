Feature: Pools

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool B"
    And the user is customer of pool "Pool B"
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"

  Scenario: Pools index
    When I log in as the user
    And I visit "/app/borrow/inventory-pools"
    And I see the page title "Inventory Pools"

    Then I see the following lines in the "Available inventory pools" section:
      | title  | body                | foot |
      | Pool A | No reservable items |      |
      | Pool B | No reservable items |      |

    When I click on "Pool A"

    Then I see the page title "Pool A"
    And I see the text:
      """
      No reservable items
      """
    And I see the "E-mail" section


