Feature: Rentals - Show - Cancel rental

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And there is a model "DSLR Camera"
    And there is a model "Tripod"
    And the following items exist:
      | code | model       | pool   |
      | A1   | DSLR Camera | Pool A |
      | A2   | Tripod      | Pool A |

  Scenario: Cancelling an unapproved order
    Given a customer order with title "My Order" and the following reservations exists for the user:
      | user | quantity | model       | pool   | start-date | end-date   | state     |
      | user | 1        | Tripod      | Pool A | 2101-02-01 | 2101-02-02 | submitted |
      | user | 1        | DSLR Camera | Pool A | 2101-02-01 | 2101-02-02 | submitted |

    When I log in as the user
    And I visit "/app/borrow/rentals/"
    And I click on "My Order"

    Then I see the page title "My Order"
    And I see the following status rows in the "State" section:
      | title    | progressbar | info                  |
      | Approval | [0 of 2]    | 0 of 2 items approved |

    When I click on "Cancel rental"
    And I accept the "Cancel rental" dialog
    And the "Cancel rental" dialog has closed

    Then I see the page title "My Order"
    And I see the following status rows in the "State" section:
      | title               | progressbar | info |
      | Rental was canceled |             |      |
    And the "Cancel rental" button is not visible

  Scenario: Where cancelling is not possible
    Given a customer order with title "My Order" and the following reservations exists for the user:
      | user | quantity | model       | pool   | start-date | end-date   | state    |
      | user | 1        | Tripod      | Pool A | 2101-02-01 | 2101-02-02 | approved |
      | user | 1        | DSLR Camera | Pool A | 2101-02-01 | 2101-02-02 | approved |

    When I log in as the user
    And I visit "/app/borrow/rentals/"
    And I click on "My Order"

    Then I see the page title "My Order"
    And the "Cancel rental" button is not visible
