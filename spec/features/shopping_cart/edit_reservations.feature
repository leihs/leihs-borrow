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
      | quantity | model       | pool   | start-date | end-date   |
      | 1        | DSLR Camera | Pool A | 2032-02-01 | 2032-02-03 |
      | 1        | Tripod      | Pool A | 2032-02-01 | 2032-02-02 |
    And I log in as the user
    When I navigate to the cart
    Then I see the following lines in the "Items" section:
      | title          | body   | foot                 |
      | 1× DSLR Camera | Pool A | 3 days from 01/02/32 |
      | 1× Tripod      | Pool A | 2 days from 01/02/32 |

    When I click on "3 days from 01/02/32"
    And I see the "Edit reservation" dialog
    # FIXME: currently this takes very long because calendar load *twice*!
    # And the calendar has finished loading
    And I see a form inside the dialog
    Then the form has exactly these fields:
      | label          | value           |
      | Quantity       | 1               |
      | Inventory Pool | Pool A (max. 3) |
      | From           | 02/01/2032      |
      | Until          | 02/03/2032      |

    When I enter "3" in the "Quantity" field
    Then the "Quantity" field has "3"
    When I click on "Plus"
    Then the "Quantity" field has "4"
    But the form has an error message:
      """
      Item is not available in the desired quantity during this period
      """

    When I click on "Minus"
    Then the "Quantity" field has "3"
    And the form has no error message

    When I click on "Confirm"
    And the "Edit reservation" dialog has closed
    # FIXME: fix reloading of cart:
    And I wait 1 seconds
    Then I see the following lines in the "Items" section:
      | title          | body   | foot                 |
      | 3× DSLR Camera | Pool A | 3 days from 01/02/32 |
      | 1× Tripod      | Pool A | 2 days from 01/02/32 |

    When I click on "Confirm rental"
    And I enter "My Movie" in the "Title" field
    And I click on "Confirm"
    Then I have been redirected to the newly created order
    Then the newly created order in the DB has:
      | title    | purpose  |
      | My Movie | My Movie |
