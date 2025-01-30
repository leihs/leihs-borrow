Feature: Rentals

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
    And the following items exist:
      | code | model       | pool   |
      | A1   | DSLR Camera | Pool A |
      | B1   | DSLR Camera | Pool B |
      | B2   | Tripod      | Pool B |

  Scenario: Switching profile
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | user | quantity | model       | pool   | relative-start-date | relative-end-date | state    |
      | user | 1        | DSLR Camera | Pool A | ${Date.today}       | ${Date.tomorrow}  | approved |
    And a customer order with title "Order 2" and the following reservations exists for the user:
      | user         | quantity | model       | pool   | relative-start-date | relative-end-date   | state    |
      | Delegation D | 1        | DSLR Camera | Pool A | ${30.days.from_now} | ${31.days.from_now} | approved |

    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see the following lines in the page content:
      | title                         |
      | 1× DSLR Camera\nPick up today |
    When I click on "Active orders"
    Then I see the following lines in the page content:
      | title   |
      | Order 1 |

    When I click on the user profile button
    And I select "Delegation D" from "Switch Profile"
    Then the user profile button shows "DD"
    And I see the following lines in the page content:
      | title   |
      | Order 2 |
    When I click on "Current lendings"
    Then I see the following lines in the page content:
      | title                              |
      | 1× DSLR Camera\nPick up in 30 days |

  Scenario: Filtering according date (until date only)

    If only until date is given, then -infinity (or a date representing that) is implied for from date.
    For orders with state TO_RETURN or RETURNED, the pickup date (or the created date of contract) is
    taken as from date.

    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | quantity | model       | pool   | start-date | end-date   | state  | pickup-date |
      | 1        | DSLR Camera | Pool A | 1900-01-01 | 1900-01-02 | closed | 1900-01-01  |
    And a customer order with title "Order 2" and the following reservations exists for the user:
      | quantity | model       | pool   | start-date | relative-end-date | state  | pickup-date |
      | 1        | DSLR Camera | Pool A | 1900-01-01 | ${Date.tomorrow}  | signed | 1900-01-01  |
    And a customer order with title "Order 3" and the following reservations exists for the user:
      | quantity | model       | pool   | start-date | relative-end-date  | state  | pickup-date |
      | 1        | DSLR Camera | Pool A | 1900-01-01 | ${7.days.from_now} | signed | today       |
    And a customer order with title "Order 4" and the following reservations exists for the user:
      | quantity | model       | pool   | relative-start-date   | relative-end-date      | state    | pickup-date |
      | 1        | DSLR Camera | Pool A | ${9999.days.from_now} | ${10000.days.from_now} | approved |             |

    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see the following lines in the page content:
      | title                                 |
      | 1× DSLR Camera\nReturn tomorrow       |
      | 1× DSLR Camera\nReturn in 7 days      |
      | 1× DSLR Camera\nPick up in 9,999 days |
    When I click on "Active orders"
    Then I see the following lines in the page content:
      | title   |
      | Order 4 |
      | Order 3 |
      | Order 2 |
    When I click on "Closed orders"
    Then I see the following lines in the page content:
      | title   |
      | Order 1 |

    When I click on "Current lendings"
    And I click on "Timespan from/until"
    And I enter "day after tomorrow" in the "Until" field
    And I click on "Apply"
    Then I see the following lines in the page content:
      | title                            |
      | 1× DSLR Camera\nReturn tomorrow  |
      | 1× DSLR Camera\nReturn in 7 days |
    When I click on "Active orders"
    Then I see the following lines in the page content:
      | title   |
      | Order 3 |
      | Order 2 |
    When I click on "Closed orders"
    Then I see the following lines in the page content:
      | title   |
      | Order 1 |

  Scenario: Filtering according to inventory pool
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | quantity | model       | pool   | relative-start-date | relative-end-date | state    |
      | 1        | DSLR Camera | Pool A | ${Date.today}       | ${Date.tomorrow}  | approved |
    And a customer order with title "Order 2" and the following reservations exists for the user:
      | quantity | model       | pool   | relative-start-date | relative-end-date  | state     |
      | 1        | DSLR Camera | Pool A | ${30.day.from_now}  | ${31.day.from_now} | approved  |
      | 1        | Tripod      | Pool B | ${30.day.from_now}  | ${31.day.from_now} | submitted |
    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see the following lines in the page content:
      | title                              |
      | 1× DSLR Camera\nPick up today      |
      | 1× DSLR Camera\nPick up in 30 days |
    When I click on "Active orders"
    Then I see the following lines in the page content:
      | title   |
      | Order 2 |
      | Order 1 |

    When I click on "Current lendings"
    And I select "Pool A" from "Inventory pools"
    Then the "Inventory pools" select field contains value "Pool A"
    And I see the following lines in the page content:
      | title                              |
      | 1× DSLR Camera\nPick up today      |
      | 1× DSLR Camera\nPick up in 30 days |
    When I click on "Active orders"
    Then I see the following lines in the page content:
      | title   |
      | Order 2 |
      | Order 1 |

    When I click on "Current lendings"
    And I select "Pool B" from "Inventory pools"
    Then the "Inventory pools" select field contains value "Pool B"
    And I see the text:
      """
      No results found for the current search filter
      """
    When I click on "Active orders"
    Then I see the following lines in the page content:
      | title   |
      | Order 2 |

  Scenario: Filtering according to search term
    Given a customer order with title "Boogie" and the following reservations exists for the user:
      | quantity | model       | pool   | relative-start-date | relative-end-date | state    |
      | 1        | DSLR Camera | Pool A | ${Date.today}       | ${Date.tomorrow}  | approved |
    And a customer order with title "Woogie" and the following reservations exists for the user:
      | quantity | model       | pool   | relative-start-date | relative-end-date  | state     |
      | 1        | DSLR Camera | Pool A | ${30.day.from_now}  | ${31.day.from_now} | approved  |
      | 1        | Tripod      | Pool B | ${30.day.from_now}  | ${31.day.from_now} | submitted |
    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see the following lines in the page content:
      | title                              |
      | 1× DSLR Camera\nPick up today      |
      | 1× DSLR Camera\nPick up in 30 days |
    When I click on "Active orders"
    Then I see the following lines in the page content:
      | title  |
      | Woogie |
      | Boogie |

    When I click on "Current lendings"
    And I enter "Boogie" in the "Search term" field
    And I click on "Search"
    Then I see the following lines in the page content:
      | title                         |
      | 1× DSLR Camera\nPick up today |
    When I click on "Active orders"
    Then I see the following lines in the page content:
      | title  |
      | Boogie |
