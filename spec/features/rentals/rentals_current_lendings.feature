Feature: Rentals - Current lendings

  Background:
    Given there is a user
    And there is a delegation "Delegation D"
    And the user is member of delegation "Delegation D"
    And there is an inventory pool "Zoo"
    And there is a model "Tiger"
    And there is a model "Lion"
    And there is a model "Serval"

  Scenario: Lendings which are NOT current
    # not approved yet
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | user | quantity | model | pool | relative-start-date | relative-end-date | state     |
      | user | 1        | Lion  | Zoo  | ${Date.today}       | ${Date.tomorrow}  | submitted |
    # expired
    Given a customer order with title "Order 2" and the following reservations exists for the user:
      | user | quantity | model | pool | relative-start-date | relative-end-date | state    |
      | user | 1        | Lion  | Zoo  | ${Date.yesterday}   | ${Date.yesterday} | approved |
    # returned
    Given a customer order with title "Order 3" and the following reservations exists for the user:
      | user | quantity | model | pool | relative-start-date | relative-end-date | state  |
      | user | 1        | Lion  | Zoo  | ${Date.yesterday}   | ${Date.yesterday} | closed |

    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see "No current lendings"
    And I don't see "Lion"
    And I don't see the selector ".ui-urgent-lendings-badge"

  Scenario: Lendings which are current, but not urgent
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | user | quantity | model  | pool | relative-start-date  | relative-end-date  | state    |
      | user | 1        | Serval | Zoo  | ${6.days.from_now}   | ${6.days.from_now} | approved |
      | user | 1        | Serval | Zoo  | ${-10.days.from_now} | ${6.days.from_now} | signed   |

    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see the following lines in the page content:
      | title                        | body                                                                          |
      | 1× Serval\nReturn in 6 days  | Zoo\n${format_date_range_short(-10.days.from_now, 6.days.from_now)} (17 days) |
      | 1× Serval\nPick up in 6 days | Zoo\n${format_date_range_short(6.days.from_now, 6.days.from_now)} (1 day)     |
    But I don't see the selector ".ui-urgent-lendings-badge"

  Scenario: Action in 2-5 days (blue)
    # This one will show in the list, but must not be counted in the status badge
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | user | quantity | model  | pool | relative-start-date | relative-end-date  | state    |
      | user | 1        | Serval | Zoo  | ${6.days.from_now}  | ${6.days.from_now} | approved |

    # These are blue
    Given a customer order with title "Order 2" and the following reservations exists for the user:
      | user | quantity | model | pool | relative-start-date | relative-end-date   | state    |
      | user | 1        | Tiger | Zoo  | ${2.days.from_now}  | ${10.days.from_now} | approved |
      | user | 1        | Tiger | Zoo  | ${3.days.from_now}  | ${10.days.from_now} | approved |
      | user | 1        | Tiger | Zoo  | ${Date.yesterday}   | ${4.days.from_now}  | signed   |
      | user | 1        | Tiger | Zoo  | ${Date.yesterday}   | ${5.days.from_now}  | signed   |

    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see the following lines in the page content:
      | title                        | body                                                                        |
      | 1× Tiger\nPick up in 2 days  | Zoo\n${format_date_range_short(2.days.from_now, 10.days.from_now)} (9 days) |
      | 1× Tiger\nPick up in 3 days  | Zoo\n${format_date_range_short(3.days.from_now, 10.days.from_now)} (8 days) |
      | 1× Tiger\nReturn in 4 days   | Zoo\n${format_date_range_short(Date.yesterday, 4.days.from_now)} (6 days)   |
      | 1× Tiger\nReturn in 5 days   | Zoo\n${format_date_range_short(Date.yesterday, 5.days.from_now)} (7 days)   |
      | 1× Serval\nPick up in 6 days | Zoo\n${format_date_range_short(6.days.from_now, 6.days.from_now)} (1 day)   |
    And I see the text "Pick up in 2 days" with selector ".text-primary"
    And I see the text "Pick up in 3 days" with selector ".text-primary"
    And I see the text "Return in 4 days" with selector ".text-primary"
    And I see the text "Return in 5 days" with selector ".text-primary"
    And I see the text "4" with selector ".ui-urgent-lendings-badge.circle-badge--primary"

    When I resize the window to mobile size
    Then I see the text "4" with selector ".ui-urgent-lendings-badge.circle-badge--primary"

  Scenario: Action today or tomorrow (orange)
    # These will show in the list, but must not be counted in the status badge
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | user | quantity | model  | pool | relative-start-date | relative-end-date   | state    |
      | user | 1        | Serval | Zoo  | ${6.days.from_now}  | ${6.days.from_now}  | approved |
      | user | 1        | Serval | Zoo  | ${2.days.from_now}  | ${10.days.from_now} | approved |

    # These are orange
    Given a customer order with title "Order 2" and the following reservations exists for the user:
      | user | quantity | model | pool | relative-start-date | relative-end-date  | state    |
      | user | 1        | Tiger | Zoo  | ${Date.today}       | ${Date.tomorrow}   | approved |
      | user | 1        | Tiger | Zoo  | ${Date.tomorrow}    | ${2.days.from_now} | approved |
      | user | 1        | Tiger | Zoo  | ${Date.yesterday}   | ${Date.tomorrow}   | signed   |

    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see the following lines in the page content:
      | title                        | body                                                                        |
      | 1× Tiger\nPick up today      | Zoo\n${format_date_range_short(Date.today, Date.tomorrow)} (2 days)         |
      | 1× Tiger\nReturn tomorrow    | Zoo\n${format_date_range_short(Date.yesterday, Date.tomorrow)} (3 days)     |
      | 1× Tiger\nPick up tomorrow   | Zoo\n${format_date_range_short(Date.tomorrow, 2.days.from_now)} (2 days)    |
      | 1× Serval\nPick up in 2 days | Zoo\n${format_date_range_short(2.days.from_now, 10.days.from_now)} (9 days) |
      | 1× Serval\nPick up in 6 days | Zoo\n${format_date_range_short(6.days.from_now, 6.days.from_now)} (1 day)   |
    And I see the text "Pick up today" with selector ".text-warning"
    And I see the text "Pick up tomorrow" with selector ".text-warning"
    And I see the text "Return tomorrow" with selector ".text-warning"
    And I see the text "Pick up in 2 days" with selector ".text-primary"
    And I see the text "3" with selector ".ui-urgent-lendings-badge.circle-badge--warning"

  Scenario: Action overdue (red)
    # These will show in the list, but must not be counted in the status badge
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | user | quantity | model  | pool | relative-start-date | relative-end-date   | state    |
      | user | 1        | Serval | Zoo  | ${6.days.from_now}  | ${6.days.from_now}  | approved |
      | user | 1        | Serval | Zoo  | ${2.days.from_now}  | ${10.days.from_now} | approved |
      | user | 1        | Serval | Zoo  | ${Date.today}       | ${Date.tomorrow}    | approved |

    # red
    Given a customer order with title "Order 2" and the following reservations exists for the user:
      | user | quantity | model | pool | relative-start-date | relative-end-date | state    |
      | user | 1        | Tiger | Zoo  | ${Date.yesterday}   | ${Date.today}     | approved |
      | user | 1        | Tiger | Zoo  | ${Date.yesterday}   | ${Date.yesterday} | signed   |

    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see the following lines in the page content:
      | title                        | body                                                                        |
      | 1× Tiger\nReturn overdue     | Zoo\n${format_date_range_short(Date.yesterday, Date.yesterday)} (1 day)     |
      | 1× Tiger\nPick up overdue    | Zoo\n${format_date_range_short(Date.yesterday, Date.today)} (2 days)        |
      | 1× Serval\nPick up today     | Zoo\n${format_date_range_short(Date.today, Date.tomorrow)} (2 days)         |
      | 1× Serval\nPick up in 2 days | Zoo\n${format_date_range_short(2.days.from_now, 10.days.from_now)} (9 days) |
      | 1× Serval\nPick up in 6 days | Zoo\n${format_date_range_short(6.days.from_now, 6.days.from_now)} (1 day)   |
    And I see the text "Return overdue" with selector ".text-danger"
    And I see the text "Pick up overdue" with selector ".text-danger"
    And I see the text "Pick up today" with selector ".text-warning"
    And I see the text "Pick up in 2 days" with selector ".text-primary"
    And I see the text "2" with selector ".ui-urgent-lendings-badge.circle-badge--danger"

  Scenario: Switching profile
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | user | quantity | model | pool | relative-start-date | relative-end-date | state    |
      | user | 1        | Tiger | Zoo  | ${Date.today}       | ${Date.tomorrow}  | approved |
    And a customer order with title "Order 2" and the following reservations exists for the user:
      | user         | quantity | model | pool | relative-start-date | relative-end-date  | state    |
      | Delegation D | 1        | Lion  | Zoo  | ${2.days.from_now}  | ${3.days.from_now} | approved |

    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see the following lines in the page content:
      | title                   |
      | 1× Tiger\nPick up today |
    And I see the text "Pick up today" with selector ".text-warning"
    And I see the text "1" with selector ".ui-urgent-lendings-badge.circle-badge--warning"

    When I click on the user profile button
    And I select "Delegation D" from "Switch Profile"
    Then the user profile button shows "DD"
    And I see the following lines in the page content:
      | title                      |
      | 1× Lion\nPick up in 2 days |
    And I see the text "Pick up in 2 days" with selector ".text-primary"
    And I see the text "1" with selector ".ui-urgent-lendings-badge.circle-badge--primary"
