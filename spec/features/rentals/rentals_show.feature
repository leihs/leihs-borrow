Feature: Rentals - Show

  Background:
    Given there is an initial admin
    And there is a user
    And there is a delegation "Delegation D"
    And the user is member of delegation "Delegation D"
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And there is an inventory pool "Pool B"
    And the user is customer of pool "Pool B"
    And there is a model "DSLR Camera"
    And there is a model "Tripod"
    And there is a model "Xylophone"
    And the following items exist:
      | code | model       | pool   |
      | A1   | DSLR Camera | Pool A |
      | B1   | DSLR Camera | Pool B |
      | B2   | Tripod      | Pool B |
      | B3   | Xylophone   | Pool B |
      | B4   | DSLR Camera | Pool B |


  Scenario: General example (2 approved items)
    Given a customer order with title "My Order" and the following reservations exists for the user:
      | user | quantity | model       | pool   | start-date | end-date | state    |
      | user | 1        | Tripod      | Pool B | today      | tomorrow | approved |
      | user | 1        | DSLR Camera | Pool A | today      | tomorrow | approved |

    When I log in as the user
    And I visit "/app/borrow/rentals/"
    And I click on "My Order"

    Then I see the page title "My Order"
    And the page subtitle is "Between ${Date.today} and ${Date.tomorrow}, 2 items"
    And I see the following status rows in the "State" section:
      | title  | progressbar | info                   |
      | Pickup | [0 of 2]    | 0 of 2 items picked up |
    And I see the "Purpose" section
    And I see the following lines in the "Items" section:
      | title          | body   | foot                                 |
      | 1× DSLR Camera | Pool A | 2 days from ${Date.today} To pick up |
      | 1× Tripod      | Pool B | 2 days from ${Date.today} To pick up |
    And I see the "Delegation" section


  Scenario: Sort order of the Reservation Lines

    Lines are sorted by start-date, then name of Pool, then name of Model.
    Unlike in the shopping cart each single item is listed (no grouping by same period, pool and model)

    Given a customer order with title "My Order" and the following reservations exists for the user:
      | user | quantity | model       | pool   | start-date | end-date   | state    |
      | user | 1        | DSLR Camera | Pool A | 2101-02-01 | 2101-02-02 | approved |
      | user | 1        | DSLR Camera | Pool A | 2101-04-01 | 2101-04-02 | approved |
      | user | 1        | DSLR Camera | Pool B | 2101-02-01 | 2101-02-02 | approved |
      | user | 1        | DSLR Camera | Pool B | 2101-03-01 | 2101-03-02 | approved |
      | user | 1        | Xylophone   | Pool B | 2101-03-01 | 2101-03-02 | approved |
      | user | 2        | Tripod      | Pool B | 2101-02-01 | 2101-02-02 | approved |

    When I log in as the user
    And I visit "/app/borrow/rentals/"
    And I click on "My Order"

    Then I see the following lines in the "Items" section:
      | title          | body   | foot                            |
      | 1× DSLR Camera | Pool A | 2 days from 01/02/01 To pick up |
      | 1× DSLR Camera | Pool B | 2 days from 01/02/01 To pick up |
      | 1× Tripod      | Pool B | 2 days from 01/02/01 To pick up |
      | 1× Tripod      | Pool B | 2 days from 01/02/01 To pick up |
      | 1× DSLR Camera | Pool B | 2 days from 01/03/01 To pick up |
      | 1× Xylophone   | Pool B | 2 days from 01/03/01 To pick up |
      | 1× DSLR Camera | Pool A | 2 days from 01/04/01 To pick up |


  Scenario: Status: 2 picked up items
    Given a customer order with title "My Order" and the following reservations exists for the user:
      | user | quantity | model       | pool   | start-date | end-date | state  |
      | user | 1        | Tripod      | Pool B | today      | tomorrow | signed |
      | user | 1        | DSLR Camera | Pool A | today      | tomorrow | signed |

    When I log in as the user
    And I visit "/app/borrow/rentals/"
    And I click on "My Order"
    Then I see the page title "My Order"
    And the page subtitle is "Between ${Date.today} and ${Date.tomorrow}, 2 items"
    And I see the following status rows in the "State" section:
      | title  | progressbar | info                  |
      | Return | [0 of 2]    | 0 of 2 items returned |
    And I see the following lines in the "Items" section:
      | title          | body   | foot                                                       |
      | 1× DSLR Camera | Pool A | 2 days from ${Date.today} To return until ${Date.tomorrow} |
      | 1× Tripod      | Pool B | 2 days from ${Date.today} To return until ${Date.tomorrow} |
