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
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | user | quantity | model       | pool   | relative-start-date | relative-end-date | state    |
      | user | 1        | Tripod      | Pool B | ${Date.today}       | ${Date.tomorrow}  | approved |
      | user | 1        | DSLR Camera | Pool A | ${Date.today}       | ${Date.tomorrow}  | approved |

    When I log in as the user
    And I visit "/app/borrow/rentals/"
    And I click on "Order 1"

    Then I see the page title "Order 1"
    And the page subtitle is "Between ${Date.today} and ${Date.tomorrow}, 2 items"
    And I see the following status rows in the "State" section:
      | title  | progressbar | info                   |
      | Pickup | [0 of 2]    | 0 of 2 items picked up |
    And I see the "Purpose" section
    And I see the following lines in the "Items" section:
      | title          | body   | foot                                 |
      | 1× DSLR Camera | Pool A | 2 days from ${Date.today} To pick up |
      | 1× Tripod      | Pool B | 2 days from ${Date.today} To pick up |
    And I see the "Order for" section


  Scenario: Sort order of the Reservation Lines

    Lines are sorted by start-date, then name of Pool, then name of Model.
    Unlike in the shopping cart each single item is listed (no grouping by same period, pool and model)

    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | user | quantity | model       | pool   | relative-start-date | relative-end-date   | state    |
      | user | 1        | DSLR Camera | Pool A | ${Date.today}       | ${Date.tomorrow}    | approved |
      | user | 1        | DSLR Camera | Pool A | ${60.days.from_now} | ${61.days.from_now} | approved |
      | user | 1        | DSLR Camera | Pool B | ${Date.today}       | ${Date.tomorrow}    | approved |
      | user | 1        | DSLR Camera | Pool B | ${30.days.from_now} | ${31.days.from_now} | approved |
      | user | 1        | Xylophone   | Pool B | ${30.days.from_now} | ${31.days.from_now} | approved |
      | user | 2        | Tripod      | Pool B | ${Date.today}       | ${Date.tomorrow}    | approved |

    When I log in as the user
    And I visit "/app/borrow/rentals/"
    And I click on "Order 1"

    Then I see the following lines in the "Items" section:
      | title          | body   | foot                                       |
      | 1× DSLR Camera | Pool A | 2 days from ${Date.today} To pick up       |
      | 1× DSLR Camera | Pool B | 2 days from ${Date.today} To pick up       |
      | 1× Tripod      | Pool B | 2 days from ${Date.today} To pick up       |
      | 1× Tripod      | Pool B | 2 days from ${Date.today} To pick up       |
      | 1× DSLR Camera | Pool B | 2 days from ${30.days.from_now} To pick up |
      | 1× Xylophone   | Pool B | 2 days from ${30.days.from_now} To pick up |
      | 1× DSLR Camera | Pool A | 2 days from ${60.days.from_now} To pick up |


  Scenario: Status: 2 picked up items
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | user | quantity | model       | pool   | relative-start-date | relative-end-date | state  |
      | user | 1        | Tripod      | Pool B | ${Date.today}       | ${Date.tomorrow}  | signed |
      | user | 1        | DSLR Camera | Pool A | ${Date.today}       | ${Date.tomorrow}  | signed |

    When I log in as the user
    And I visit "/app/borrow/rentals/"
    And I click on "Order 1"
    Then I see the page title "Order 1"
    And the page subtitle is "Between ${Date.today} and ${Date.tomorrow}, 2 items"
    And I see the following status rows in the "State" section:
      | title  | progressbar | info                  |
      | Return | [0 of 2]    | 0 of 2 items returned |
    And I see the following lines in the "Items" section:
      | title          | body   | foot                                                       |
      | 1× DSLR Camera | Pool A | 2 days from ${Date.today} To return until ${Date.tomorrow} |
      | 1× Tripod      | Pool B | 2 days from ${Date.today} To return until ${Date.tomorrow} |
