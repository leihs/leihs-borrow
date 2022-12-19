Feature: Shopping Cart - Editing Reservations
  Reservations in the Cart can be edited with an UI similar to creating the Reservation.
  The quantity, inventory pool, start- and end-dates can be edited,
  or the whole reservation can be deleted.

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And there is a model "DSLR Camera"
    And there is a model "Tripod"
    And the following items exist:
      | code | model       | pool   |
      | A11  | DSLR Camera | Pool A |
      | A12  | DSLR Camera | Pool A |
      | A13  | DSLR Camera | Pool A |
      | A21  | Tripod      | Pool A |

  Scenario: Editing a the quantity of a reservation

    Given the following reservations exist for the user:
      | quantity | model       | pool   | relative-start-date | relative-end-date  |
      | 1        | DSLR Camera | Pool A | ${Date.today}       | ${2.days.from_now} |
      | 1        | Tripod      | Pool A | ${Date.today}       | ${1.day.from_now}  |
    And I log in as the user
    When I navigate to the cart
    Then I see the following lines in the "Items" section:
      | title          | body   | foot                      |
      | 1× DSLR Camera | Pool A | 3 days from ${Date.today} |
      | 1× Tripod      | Pool A | 2 days from ${Date.today} |

    When I click on the card with title "1× DSLR Camera"
    And I see the "Edit reservation" dialog
    And I see a form inside the dialog
    Then the form has exactly these fields:
      | label                         | value              |
      | Inventory pool                | Pool A (max. 3)    |
      | Quantity                      | 1                  |
      | From                          | ${Date.today}      |
      | Until                         | ${2.days.from_now} |
      | Show availability in calendar | on                 |

    When I enter "3" in the "Quantity" field
    Then the "Quantity" field has "3"
    When I click on "+"
    Then the "Quantity" field has "4"
    But the form has an error message:
      """
      Item is not available in the desired quantity during this period
      """

    When I click on "-"
    Then the "Quantity" field has "3"
    And the form has no error message

    When I click on "Confirm"
    And the "Edit reservation" dialog has closed
    Then I see the following lines in the "Items" section:
      | title          | body   | foot                      |
      | 3× DSLR Camera | Pool A | 3 days from ${Date.today} |
      | 1× Tripod      | Pool A | 2 days from ${Date.today} |

    When I click on "Send order"
    And I enter "My Movie" in the "Title" field
    And I click on "Send"
    And the "Send order" dialog has closed
    And I accept the "Order submitted" dialog with the text:
      """
      Order was submitted but still needs to be approved!
      My Movie
      Between ${Date.today} and ${2.days.from_now}, 4 items
      """
    Then I have been redirected to the orders list
    Then the newly created order in the DB has:
      | title    | purpose  |
      | My Movie | My Movie |

  Scenario: Deleting a reservation

    Given the following reservations exist for the user:
      | quantity | model       | pool   | relative-start-date | relative-end-date  |
      | 1        | DSLR Camera | Pool A | ${Date.today}       | ${2.days.from_now} |
      | 1        | Tripod      | Pool A | ${Date.today}       | ${Date.tomorrow}   |
    And I log in as the user
    When I navigate to the cart
    Then I see the following lines in the "Items" section:
      | title          | body   | foot                      |
      | 1× DSLR Camera | Pool A | 3 days from ${Date.today} |
      | 1× Tripod      | Pool A | 2 days from ${Date.today} |

    When I click on the card with title "1× DSLR Camera"
    And I see the "Edit reservation" dialog
    And I click on "Remove reservation"
    And the "Edit reservation" dialog has closed
    Then I see the following lines in the "Items" section:
      | title     | body   | foot                      |
      | 1× Tripod | Pool A | 2 days from ${Date.today} |
