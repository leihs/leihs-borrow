Feature: Rentals - Show - Status

  Tests for the relevant status situations.
  - Status summary (as specified in Borrow UI Storybook - Prototypes/Orders/Status Summary)
  - Status badges in the reservation list

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And there is an inventory pool "Pool B"
    And the user is customer of pool "Pool B"
    And there is an inventory pool "Pool C"
    And the user is customer of pool "Pool C"
    And there is a model "Elefant"
    And the following items exist:
      | code  | model   | pool   |
      | A-001 | Elefant | Pool A |
      | B-001 | Elefant | Pool B |
      | C-001 | Elefant | Pool C |
  # (note that the factory does not reference individual items, so its enough to have 1 per model and pool)

  Scenario: Story: Typical flow
    Given a customer order with title "New order" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state     |
      | user | 3        | Elefant | Pool A | ${Date.tomorrow}    | ${2.day.from_now} | submitted |
    And a customer order with title "Approved order" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state    |
      | user | 3        | Elefant | Pool A | ${Date.tomorrow}    | ${2.day.from_now} | approved |
    And a customer order with title "Some picked up" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state    |
      | user | 2        | Elefant | Pool A | ${Date.today}       | ${Date.tomorrow}  | approved |
      | user | 1        | Elefant | Pool A | ${Date.today}       | ${Date.tomorrow}  | signed   |
    And a customer order with title "Some picked up, some returned" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state    |
      | user | 1        | Elefant | Pool A | ${Date.today}       | ${Date.tomorrow}  | approved |
      | user | 1        | Elefant | Pool A | ${Date.today}       | ${Date.tomorrow}  | signed   |
      | user | 1        | Elefant | Pool A | ${Date.today}       | ${Date.tomorrow}  | closed   |
    And a customer order with title "All picked up, some returned" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state  |
      | user | 1        | Elefant | Pool A | ${Date.today}       | ${Date.tomorrow}  | signed |
      | user | 2        | Elefant | Pool A | ${Date.today}       | ${Date.tomorrow}  | closed |
    And a customer order with title "All returned" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state  |
      | user | 3        | Elefant | Pool A | ${Date.yesterday}   | ${Date.today}     | closed |

    And I log in as the user

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=open-orders"
    And I click on the card with title "New order"
    And I see the page title "New order"

    Then I see the following status rows in the "State" section:
      | title    | progressbar | info                  |
      | Approval | [0 of 3]    | 0 of 3 items approved |
    And I see the following lines in the "Items" section:
      | title                   | body                                                                       |
      | 1× Elefant\nIn approval | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nIn approval | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nIn approval | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=open-orders"
    And I click on the card with title "Approved order"
    And I see the page title "Approved order"

    Then I see the following status rows in the "State" section:
      | title  | progressbar | info                   |
      | Pickup | [0 of 3]    | 0 of 3 items picked up |
    And I see the following lines in the "Items" section:
      | title                        | body                                                                       |
      | 1× Elefant\nPick up tomorrow | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nPick up tomorrow | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nPick up tomorrow | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=open-orders"
    And I click on the card with title "Some picked up"
    And I see the page title "Some picked up"

    Then I see the following status rows in the "State" section:
      | title  | progressbar | info                   |
      | Pickup | [1 of 3]    | 1 of 3 items picked up |
      | Return | [0 of 3]    | 0 of 3 items returned  |
    And I see the following lines in the "Items" section:
      | title                       | body                                                                   |
      | 1× Elefant\nPick up today   | Pool A\n${format_date_range_short(Date.today, Date.tomorrow)} (2 days) |
      | 1× Elefant\nPick up today   | Pool A\n${format_date_range_short(Date.today, Date.tomorrow)} (2 days) |
      | 1× Elefant\nReturn tomorrow | Pool A\n${format_date_range_short(Date.today, Date.tomorrow)} (2 days) |

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=open-orders"
    And I click on the card with title "Some picked up, some returned"
    And I see the page title "Some picked up, some returned"

    Then I see the following status rows in the "State" section:
      | title  | progressbar | info                   |
      | Pickup | [2 of 3]    | 2 of 3 items picked up |
      | Return | [1 of 3]    | 1 of 3 items returned  |
    And I see the following lines in the "Items" section:
      | title                       | body                                                                   |
      | 1× Elefant\nPick up today   | Pool A\n${format_date_range_short(Date.today, Date.tomorrow)} (2 days) |
      | 1× Elefant\nReturn tomorrow | Pool A\n${format_date_range_short(Date.today, Date.tomorrow)} (2 days) |
      | 1× Elefant\nReturned        | Pool A\n${format_date_range_short(Date.today, Date.tomorrow)} (2 days) |

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=open-orders"
    And I click on the card with title "All picked up, some returned"
    And I see the page title "All picked up, some returned"

    Then I see the following status rows in the "State" section:
      | title  | progressbar | info                  |
      | Return | [2 of 3]    | 2 of 3 items returned |
    And I see the following lines in the "Items" section:
      | title                       | body                                                                   |
      | 1× Elefant\nReturn tomorrow | Pool A\n${format_date_range_short(Date.today, Date.tomorrow)} (2 days) |
      | 1× Elefant\nReturned        | Pool A\n${format_date_range_short(Date.today, Date.tomorrow)} (2 days) |
      | 1× Elefant\nReturned        | Pool A\n${format_date_range_short(Date.today, Date.tomorrow)} (2 days) |

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=closed-orders"
    And I click on the card with title "All returned"
    And I see the page title "All returned"

    Then I see the following status rows in the "State" section:
      | title              | progressbar | info |
      | All items returned |             |      |
    And I see the following lines in the "Items" section:
      | title                | body                                                                    |
      | 1× Elefant\nReturned | Pool A\n${format_date_range_short(Date.yesterday, Date.today)} (2 days) |
      | 1× Elefant\nReturned | Pool A\n${format_date_range_short(Date.yesterday, Date.today)} (2 days) |
      | 1× Elefant\nReturned | Pool A\n${format_date_range_short(Date.yesterday, Date.today)} (2 days) |

  Scenario: Story: Details approval

    Given a customer order with title "Canceled order" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state    |
      | user | 3        | Elefant | Pool A | ${Date.tomorrow}    | ${2.day.from_now} | canceled |
    And a customer order with title "Rejected order" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state    |
      | user | 3        | Elefant | Pool A | ${Date.tomorrow}    | ${2.day.from_now} | rejected |
    And a customer order with title "Partially approved order" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state     |
      | user | 1        | Elefant | Pool A | ${Date.tomorrow}    | ${2.day.from_now} | approved  |
      | user | 1        | Elefant | Pool B | ${Date.tomorrow}    | ${2.day.from_now} | submitted |
      | user | 1        | Elefant | Pool C | ${Date.tomorrow}    | ${2.day.from_now} | submitted |
    And a customer order with title "Some approved, some rejected, some unapproved" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state     |
      | user | 1        | Elefant | Pool A | ${Date.tomorrow}    | ${2.day.from_now} | approved  |
      | user | 1        | Elefant | Pool B | ${Date.tomorrow}    | ${2.day.from_now} | submitted |
      | user | 1        | Elefant | Pool C | ${Date.tomorrow}    | ${2.day.from_now} | rejected  |
    And a customer order with title "Some approved, some rejected" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state    |
      | user | 1        | Elefant | Pool A | ${Date.tomorrow}    | ${2.day.from_now} | approved |
      | user | 1        | Elefant | Pool B | ${Date.tomorrow}    | ${2.day.from_now} | approved |
      | user | 1        | Elefant | Pool C | ${Date.tomorrow}    | ${2.day.from_now} | rejected |
    And a customer order with title "Some expired unapproved" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state     |
      | user | 1        | Elefant | Pool A | ${Date.tomorrow}    | ${2.day.from_now} | submitted |
      | user | 1        | Elefant | Pool A | ${Date.yesterday}   | ${Date.yesterday} | submitted |
    And a customer order with title "All expired unapproved" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state     |
      | user | 2        | Elefant | Pool A | ${Date.yesterday}   | ${Date.yesterday} | submitted |

    When I log in as the user

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=closed-orders"
    And I click on the card with title "Canceled order"
    And I see the page title "Canceled order"

    Then I see the following status rows in the "State" section:
      | title              | progressbar | info |
      | Order was canceled |             |      |
    And I see the following lines in the "Items" section:
      | title                | body                                                                       |
      | 1× Elefant\nCanceled | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nCanceled | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nCanceled | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=closed-orders"
    And I click on the card with title "Rejected order"
    Then I see the page title "Rejected order"

    Then I see the following status rows in the "State" section:
      | title              | progressbar | info |
      | Order was rejected |             |      |
    And I see the following lines in the "Items" section:
      | title                | body                                                                       |
      | 1× Elefant\nRejected | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nRejected | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nRejected | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=open-orders"
    And I click on the card with title "Partially approved order"
    And I see the page title "Partially approved order"

    Then I see the following status rows in the "State" section:
      | title    | progressbar | info                   |
      | Approval | [1 of 3]    | 1 of 3 items approved  |
      | Pickup   | [0 of 3]    | 0 of 3 items picked up |
    And I see the following lines in the "Items" section:
      | title                        | body                                                                       |
      | 1× Elefant\nPick up tomorrow | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nIn approval      | Pool B\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nIn approval      | Pool C\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=open-orders"
    And I click on the card with title "Some approved, some rejected, some unapproved"
    And I see the page title "Some approved, some rejected, some unapproved"

    Then I see the following status rows in the "State" section:
      | title    | progressbar | info                               |
      | Approval | [2 of 3]    | 1 of 3 items approved (1 rejected) |
      | Pickup   | [0 of 2]    | 0 of 2 items picked up             |
    And I see the following lines in the "Items" section:
      | title                        | body                                                                       |
      | 1× Elefant\nPick up tomorrow | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nIn approval      | Pool B\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nRejected         | Pool C\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=open-orders"
    And I click on the card with title "Some approved, some rejected"
    And I see the page title "Some approved, some rejected"

    Then I see the following status rows in the "State" section:
      | title    | progressbar | info                               |
      | Approval |             | 2 of 3 items approved (1 rejected) |
      | Pickup   | [0 of 2]    | 0 of 2 items picked up             |
    And I see the following lines in the "Items" section:
      | title                        | body                                                                       |
      | 1× Elefant\nPick up tomorrow | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nPick up tomorrow | Pool B\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nRejected         | Pool C\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=open-orders"
    And I click on the card with title "Some expired unapproved"
    And I see the page title "Some expired unapproved"

    Then I see the following status rows in the "State" section:
      | title    | progressbar | info                              |
      | Approval | [1 of 2]    | 0 of 2 items approved (1 expired) |
    And I see the following lines in the "Items" section:
      | title                              | body                                                                       |
      | 1× Elefant\nExpired (not approved) | Pool A\n${format_date_range_short(Date.yesterday, Date.yesterday)} (1 day) |
      | 1× Elefant\nIn approval            | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=closed-orders"
    And I click on the card with title "All expired unapproved"
    And I see the page title "All expired unapproved"

    Then I see the following status rows in the "State" section:
      | title                  | progressbar | info |
      | Expired (not approved) |             |      |
    And I see the following lines in the "Items" section:
      | title                              | body                                                                       |
      | 1× Elefant\nExpired (not approved) | Pool A\n${format_date_range_short(Date.yesterday, Date.yesterday)} (1 day) |
      | 1× Elefant\nExpired (not approved) | Pool A\n${format_date_range_short(Date.yesterday, Date.yesterday)} (1 day) |

  Scenario: Story: Details pickup

    Given a customer order with title "Expired order" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state    |
      | user | 3        | Elefant | Pool A | ${Date.yesterday}   | ${Date.yesterday} | approved |
    And a customer order with title "Some expired, some picked-up" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state    |
      | user | 1        | Elefant | Pool A | ${Date.yesterday}   | ${Date.yesterday} | approved |
      | user | 1        | Elefant | Pool A | ${Date.tomorrow}    | ${2.day.from_now} | approved |
      | user | 1        | Elefant | Pool A | ${Date.tomorrow}    | ${2.day.from_now} | signed   |
    And a customer order with title "Some expired, all others picked-up" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state    |
      | user | 1        | Elefant | Pool A | ${Date.yesterday}   | ${Date.yesterday} | approved |
      | user | 2        | Elefant | Pool A | ${Date.tomorrow}    | ${2.day.from_now} | signed   |

    And I log in as the user

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=closed-orders"
    And I click on the card with title "Expired order"
    And I see the page title "Expired order"

    Then I see the following status rows in the "State" section:
      | title                   | progressbar | info |
      | Expired (not picked up) |             |      |
    And I see the following lines in the "Items" section:
      | title                               | body                                                                       |
      | 1× Elefant\nExpired (not picked up) | Pool A\n${format_date_range_short(Date.yesterday, Date.yesterday)} (1 day) |
      | 1× Elefant\nExpired (not picked up) | Pool A\n${format_date_range_short(Date.yesterday, Date.yesterday)} (1 day) |
      | 1× Elefant\nExpired (not picked up) | Pool A\n${format_date_range_short(Date.yesterday, Date.yesterday)} (1 day) |

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=open-orders"
    And I click on the card with title "Some expired, some picked-up"
    And I see the page title "Some expired, some picked-up"

    Then I see the following status rows in the "State" section:
      | title  | progressbar | info                               |
      | Pickup | [2 of 3]    | 1 of 3 items picked up (1 expired) |
      | Return | [0 of 2]    | 0 of 2 items returned              |
    And I see the following lines in the "Items" section:
      | title                               | body                                                                       |
      | 1× Elefant\nExpired (not picked up) | Pool A\n${format_date_range_short(Date.yesterday, Date.yesterday)} (1 day) |
      | 1× Elefant\nPick up tomorrow        | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nReturn in 2 days        | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=open-orders"
    And I click on the card with title "Some expired, all others picked-up"
    And I see the page title "Some expired, all others picked-up"

    Then I see the following status rows in the "State" section:
      | title  | progressbar | info                               |
      | Pickup |             | 2 of 3 items picked up (1 expired) |
      | Return | [0 of 2]    | 0 of 2 items returned              |
    And I see the following lines in the "Items" section:
      | title                               | body                                                                       |
      | 1× Elefant\nExpired (not picked up) | Pool A\n${format_date_range_short(Date.yesterday, Date.yesterday)} (1 day) |
      | 1× Elefant\nReturn in 2 days        | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |
      | 1× Elefant\nReturn in 2 days        | Pool A\n${format_date_range_short(Date.tomorrow, 2.day.from_now)} (2 days) |

  Scenario: Story: Details return

    Given a customer order with title "All overdue" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state  |
      | user | 3        | Elefant | Pool A | ${Date.yesterday}   | ${Date.yesterday} | signed |
    And a customer order with title "Some overdue, some returned" and the following reservations exists for the user:
      | user | quantity | model   | pool   | relative-start-date | relative-end-date | state  |
      | user | 1        | Elefant | Pool A | ${Date.yesterday}   | ${Date.yesterday} | signed |
      | user | 1        | Elefant | Pool A | ${Date.yesterday}   | ${Date.tomorrow}  | signed |
      | user | 1        | Elefant | Pool A | ${Date.yesterday}   | ${Date.yesterday} | closed |

    And I log in as the user

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=open-orders"
    And I click on the card with title "All overdue"
    And I see the page title "All overdue"

    Then I see the following status rows in the "State" section:
      | title   | progressbar | info                              |
      | Overdue | [0 of 3]    | 0 of 3 items returned (3 overdue) |
    And I see the following lines in the "Items" section:
      | title                      | body                                                                       |
      | 1× Elefant\nReturn overdue | Pool A\n${format_date_range_short(Date.yesterday, Date.yesterday)} (1 day) |
      | 1× Elefant\nReturn overdue | Pool A\n${format_date_range_short(Date.yesterday, Date.yesterday)} (1 day) |
      | 1× Elefant\nReturn overdue | Pool A\n${format_date_range_short(Date.yesterday, Date.yesterday)} (1 day) |

    # -----------------------------------
    When I visit "/borrow/rentals/?tab=open-orders"
    And I click on the card with title "Some overdue, some returned"
    And I see the page title "Some overdue, some returned"

    Then I see the following status rows in the "State" section:
      | title   | progressbar | info                              |
      | Overdue | [1 of 3]    | 1 of 3 items returned (1 overdue) |
    And I see the following lines in the "Items" section:
      | title                       | body                                                                       |
      | 1× Elefant\nReturn overdue  | Pool A\n${format_date_range_short(Date.yesterday, Date.yesterday)} (1 day) |
      | 1× Elefant\nReturned        | Pool A\n${format_date_range_short(Date.yesterday, Date.yesterday)} (1 day) |
      | 1× Elefant\nReturn tomorrow | Pool A\n${format_date_range_short(Date.yesterday, Date.tomorrow)} (3 days) |
