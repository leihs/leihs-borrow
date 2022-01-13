Feature: Rentals - Show - Status

  Tests for the relevant status situations.
  - Status summary (as specified in Leihs UI Storybook - MobileApp/Wireframes/Meine Ausleihen/Status Summary)
  - Status badges in the reservation list

  # TODO: Add asserts for "the page subtitle is ..."

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
      | user | quantity | model   | pool   | start-date | end-date   | state     |
      | user | 3        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | submitted |
    And a customer order with title "Approved order" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state    |
      | user | 3        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | approved |
    And a customer order with title "Some picked up" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state    |
      | user | 2        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | approved |
      | user | 1        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | signed   |
    And a customer order with title "Some picked up, some returned" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state    |
      | user | 1        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | approved |
      | user | 1        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | signed   |
      | user | 1        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | closed   |
    And a customer order with title "All picked up, some returned" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state  |
      | user | 1        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | signed |
      | user | 2        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | closed |
    And a customer order with title "All returned" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state  |
      | user | 3        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | closed |

    And I log in as the user

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "New order"
    And I see the page title "New order"

    Then I see the following status rows in the "State" section:
      | title    | progressbar | info                  |
      | Approval | [0 of 3]    | 0 of 3 items approved |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                             |
      | 1× Elefant | Pool A | 2 days from 01/02/01 In approval |
      | 1× Elefant | Pool A | 2 days from 01/02/01 In approval |
      | 1× Elefant | Pool A | 2 days from 01/02/01 In approval |

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "Approved order"
    And I see the page title "Approved order"

    Then I see the following status rows in the "State" section:
      | title  | progressbar | info                   |
      | Pickup | [0 of 3]    | 0 of 3 items picked up |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                            |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To pick up |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To pick up |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To pick up |

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "Some picked up"
    And I see the page title "Some picked up"

    Then I see the following status rows in the "State" section:
      | title  | progressbar | info                   |
      | Pickup | [1 of 3]    | 1 of 3 items picked up |
      | Return | [0 of 3]    | 0 of 3 items returned  |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                                          |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To pick up               |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To pick up               |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To return until 02/02/01 |

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "Some picked up, some returned"
    And I see the page title "Some picked up, some returned"

    Then I see the following status rows in the "State" section:
      | title  | progressbar | info                   |
      | Pickup | [2 of 3]    | 2 of 3 items picked up |
      | Return | [1 of 3]    | 1 of 3 items returned  |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                                          |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To pick up               |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To return until 02/02/01 |
      | 1× Elefant | Pool A | 2 days from 01/02/01 Returned                 |

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "All picked up, some returned"
    And I see the page title "All picked up, some returned"

    Then I see the following status rows in the "State" section:
      | title  | progressbar | info                  |
      | Return | [2 of 3]    | 2 of 3 items returned |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                                          |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To return until 02/02/01 |
      | 1× Elefant | Pool A | 2 days from 01/02/01 Returned                 |
      | 1× Elefant | Pool A | 2 days from 01/02/01 Returned                 |

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "All returned"
    And I see the page title "All returned"

    Then I see the following status rows in the "State" section:
      | title              | progressbar | info |
      | All items returned |             |      |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                          |
      | 1× Elefant | Pool A | 2 days from 01/02/01 Returned |
      | 1× Elefant | Pool A | 2 days from 01/02/01 Returned |
      | 1× Elefant | Pool A | 2 days from 01/02/01 Returned |

  Scenario: Story: Details approval

    Given a customer order with title "Canceled order" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state    |
      | user | 3        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | canceled |
    And a customer order with title "Rejected order" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state    |
      | user | 3        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | rejected |
    And a customer order with title "Partially approved order" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state     |
      | user | 1        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | approved  |
      | user | 1        | Elefant | Pool B | 2101-02-01 | 2101-02-02 | submitted |
      | user | 1        | Elefant | Pool C | 2101-02-01 | 2101-02-02 | submitted |
    And a customer order with title "Some approved, some rejected, some unapproved" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state     |
      | user | 1        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | approved  |
      | user | 1        | Elefant | Pool B | 2101-02-01 | 2101-02-02 | submitted |
      | user | 1        | Elefant | Pool C | 2101-02-01 | 2101-02-02 | rejected  |
    And a customer order with title "Some approved, some rejected" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state    |
      | user | 1        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | approved |
      | user | 1        | Elefant | Pool B | 2101-02-01 | 2101-02-02 | approved |
      | user | 1        | Elefant | Pool C | 2101-02-01 | 2101-02-02 | rejected |
    And a customer order with title "Some expired unapproved" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state     |
      | user | 1        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | submitted |
      | user | 1        | Elefant | Pool A | 2000-02-01 | 2000-02-02 | submitted |
    And a customer order with title "All expired unapproved" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state     |
      | user | 2        | Elefant | Pool A | 2000-02-01 | 2000-02-02 | submitted |

    When I log in as the user

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "Canceled order"
    And I see the page title "Canceled order"

    Then I see the following status rows in the "State" section:
      | title               | progressbar | info |
      | Rental was canceled |             |      |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                          |
      | 1× Elefant | Pool A | 2 days from 01/02/01 Canceled |
      | 1× Elefant | Pool A | 2 days from 01/02/01 Canceled |
      | 1× Elefant | Pool A | 2 days from 01/02/01 Canceled |

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "Rejected order"
    Then I see the page title "Rejected order"

    Then I see the following status rows in the "State" section:
      | title               | progressbar | info |
      | Rental was rejected |             |      |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                          |
      | 1× Elefant | Pool A | 2 days from 01/02/01 Rejected |
      | 1× Elefant | Pool A | 2 days from 01/02/01 Rejected |
      | 1× Elefant | Pool A | 2 days from 01/02/01 Rejected |

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "Partially approved order"
    And I see the page title "Partially approved order"

    Then I see the following status rows in the "State" section:
      | title    | progressbar | info                   |
      | Approval | [1 of 3]    | 1 of 3 items approved  |
      | Pickup   | [0 of 3]    | 0 of 3 items picked up |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                             |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To pick up  |
      | 1× Elefant | Pool B | 2 days from 01/02/01 In approval |
      | 1× Elefant | Pool C | 2 days from 01/02/01 In approval |

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "Some approved, some rejected, some unapproved"
    And I see the page title "Some approved, some rejected, some unapproved"

    Then I see the following status rows in the "State" section:
      | title    | progressbar | info                               |
      | Approval | [2 of 3]    | 1 of 3 items approved (1 rejected) |
      | Pickup   | [0 of 2]    | 0 of 2 items picked up             |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                             |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To pick up  |
      | 1× Elefant | Pool B | 2 days from 01/02/01 In approval |
      | 1× Elefant | Pool C | 2 days from 01/02/01 Rejected    |

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "Some approved, some rejected"
    And I see the page title "Some approved, some rejected"

    Then I see the following status rows in the "State" section:
      | title    | progressbar | info                               |
      | Approval |             | 2 of 3 items approved (1 rejected) |
      | Pickup   | [0 of 2]    | 0 of 2 items picked up             |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                            |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To pick up |
      | 1× Elefant | Pool B | 2 days from 01/02/01 To pick up |
      | 1× Elefant | Pool C | 2 days from 01/02/01 Rejected   |

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "Some expired unapproved"
    And I see the page title "Some expired unapproved"

    Then I see the following status rows in the "State" section:
      | title    | progressbar | info                              |
      | Approval | [1 of 2]    | 0 of 2 items approved (1 expired) |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                                             |
      | 1× Elefant | Pool A | 2 days from 01/02/00 Not approved until 02/02/00 |
      | 1× Elefant | Pool A | 2 days from 01/02/01 In approval                 |

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "All expired unapproved"
    And I see the page title "All expired unapproved"

    Then I see the following status rows in the "State" section:
      | title                  | progressbar | info |
      | Expired (not approved) |             |      |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                                             |
      | 1× Elefant | Pool A | 2 days from 01/02/00 Not approved until 02/02/00 |
      | 1× Elefant | Pool A | 2 days from 01/02/00 Not approved until 02/02/00 |


  Scenario: Story: Details pickup

    Given a customer order with title "Expired order" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state    |
      | user | 3        | Elefant | Pool A | 2000-02-01 | 2000-02-02 | approved |
    And a customer order with title "Some expired, some picked-up" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state    |
      | user | 1        | Elefant | Pool A | 2000-02-01 | 2000-02-02 | approved |
      | user | 1        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | approved |
      | user | 1        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | signed   |
    And a customer order with title "Some expired, all others picked-up" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state    |
      | user | 1        | Elefant | Pool A | 2000-02-01 | 2000-02-02 | approved |
      | user | 2        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | signed   |

    And I log in as the user

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "Expired order"
    And I see the page title "Expired order"

    Then I see the following status rows in the "State" section:
      | title                   | progressbar | info |
      | Expired (not picked up) |             |      |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                                              |
      | 1× Elefant | Pool A | 2 days from 01/02/00 Not picked up until 02/02/00 |
      | 1× Elefant | Pool A | 2 days from 01/02/00 Not picked up until 02/02/00 |
      | 1× Elefant | Pool A | 2 days from 01/02/00 Not picked up until 02/02/00 |

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "Some expired, some picked-up"
    And I see the page title "Some expired, some picked-up"

    Then I see the following status rows in the "State" section:
      | title  | progressbar | info                               |
      | Pickup | [2 of 3]    | 1 of 3 items picked up (1 expired) |
      | Return | [0 of 2]    | 0 of 2 items returned              |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                                              |
      | 1× Elefant | Pool A | 2 days from 01/02/00 Not picked up until 02/02/00 |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To pick up                   |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To return until 02/02/01     |

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "Some expired, all others picked-up"
    And I see the page title "Some expired, all others picked-up"

    Then I see the following status rows in the "State" section:
      | title  | progressbar | info                               |
      | Pickup |             | 2 of 3 items picked up (1 expired) |
      | Return | [0 of 2]    | 0 of 2 items returned              |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                                              |
      | 1× Elefant | Pool A | 2 days from 01/02/00 Not picked up until 02/02/00 |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To return until 02/02/01     |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To return until 02/02/01     |

  Scenario: Story: Details return

    Given a customer order with title "All overdue" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state  |
      | user | 3        | Elefant | Pool A | 2000-02-01 | 2000-02-02 | signed |
    And a customer order with title "Some overdue, some returned" and the following reservations exists for the user:
      | user | quantity | model   | pool   | start-date | end-date   | state  |
      | user | 1        | Elefant | Pool A | 2000-02-01 | 2000-02-02 | signed |
      | user | 1        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | signed |
      | user | 1        | Elefant | Pool A | 2101-02-01 | 2101-02-02 | closed |

    And I log in as the user

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "All overdue"
    And I see the page title "All overdue"

    Then I see the following status rows in the "State" section:
      | title   | progressbar | info                              |
      | Overdue | [0 of 3]    | 0 of 3 items returned (3 overdue) |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                                          |
      | 1× Elefant | Pool A | 2 days from 01/02/00 To return until 02/02/00 |
      | 1× Elefant | Pool A | 2 days from 01/02/00 To return until 02/02/00 |
      | 1× Elefant | Pool A | 2 days from 01/02/00 To return until 02/02/00 |

    # -----------------------------------
    When I visit "/app/borrow/rentals/"
    And I click on "Some overdue, some returned"
    And I see the page title "Some overdue, some returned"

    Then I see the following status rows in the "State" section:
      | title   | progressbar | info                              |
      | Overdue | [1 of 3]    | 1 of 3 items returned (1 overdue) |
    And I see the following lines in the "Items" section:
      | title      | body   | foot                                          |
      | 1× Elefant | Pool A | 2 days from 01/02/00 To return until 02/02/00 |
      | 1× Elefant | Pool A | 2 days from 01/02/01 To return until 02/02/01 |
      | 1× Elefant | Pool A | 2 days from 01/02/01 Returned                 |
