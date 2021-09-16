Feature: Shopping Cart - Grouping Reservations
  Reservations are grouped into reservation lines.
  They are combined when model, start-date, end-date and inventory-pool are the same,
  otherwise multiple lines are shown (or editing a line would not be possible).

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And there is an inventory pool "Pool B"
    And the user is customer of pool "Pool B"
    And there is a model "DSLR Camera"
    And there is a model "Tripod"
    And the following items exist:
      | code  | model       | pool   |
      | A1    | DSLR Camera | Pool A |
      | A2    | DSLR Camera | Pool A |
      | B1    | DSLR Camera | Pool B |
      | B2    | Tripod      | Pool B |


  Scenario: Grouping Reservations
    Given the following reservations exist for the user:
      | quantity | model       | pool   | start-date | end-date   |
      |        1 | DSLR Camera | Pool A | 2000-02-01 | 2000-02-02 |
      |        1 | DSLR Camera | Pool A | 2000-02-01 | 2000-02-02 |
      |        1 | DSLR Camera | Pool B | 2000-02-01 | 2000-02-02 |
      |        1 | Tripod      | Pool B | 2000-02-01 | 2000-02-02 |
    When I log in as the user
    And I navigate to the cart
    Then I see the following lines in the "Items" section:
      | title          | body           |
      | 3x DSLR Camera | Pool A, Pool B |
      | 1x Tripod      | Pool B         |

