Feature: Shopping Cart - Display of Reservation Lines

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
      | code | model       | pool   |
      | A1   | DSLR Camera | Pool A |
      | A2   | DSLR Camera | Pool A |
      | A3   | DSLR Camera | Pool A |
      | B1   | DSLR Camera | Pool B |
      | B2   | Tripod      | Pool B |


  Scenario: Grouping Reservations by Model, Dates and Pool

    Reservations are grouped into reservation lines.
    They are combined when model, start-date, end-date and inventory-pool are the same,
    otherwise multiple lines are shown (or editing a line would not be possible).

    Given the following reservations exist for the user:
      | quantity | model       | pool   | start-date | end-date   |
      | 1        | DSLR Camera | Pool A | 2101-02-01 | 2101-02-02 |
      | 1        | DSLR Camera | Pool A | 2101-02-01 | 2101-02-02 |
      | 1        | DSLR Camera | Pool A | 2101-03-01 | 2101-03-02 |
      | 1        | DSLR Camera | Pool B | 2101-02-01 | 2101-02-02 |
      | 1        | Tripod      | Pool B | 2101-02-01 | 2101-02-02 |
    When I log in as the user
    And I navigate to the cart
    Then I see the following lines in the "Items" section:
      | title          | body   | foot                 |
      | 2× DSLR Camera | Pool A | 2 days from 01/02/01 |
      | 1× DSLR Camera | Pool B | 2 days from 01/02/01 |
      | 1× Tripod      | Pool B | 2 days from 01/02/01 |
      | 1× DSLR Camera | Pool A | 2 days from 01/03/01 |


  Scenario: Sort order of the Reservation Lines

    Lines are sorted by start-date, then name of Pool, then name of Model.

    Given there is a model "Xylophone"
    And the following items exist:
      | code | model       | pool   |
      | B3   | Xylophone   | Pool B |
      | B4   | DSLR Camera | Pool B |
    And the following reservations exist for the user:
      | quantity | model       | pool   | start-date | end-date   |
      | 1        | DSLR Camera | Pool A | 2101-02-01 | 2101-02-02 |
      | 1        | DSLR Camera | Pool A | 2101-04-01 | 2101-04-02 |
      | 1        | DSLR Camera | Pool B | 2101-02-01 | 2101-02-02 |
      | 1        | DSLR Camera | Pool B | 2101-03-01 | 2101-03-02 |
      | 1        | Xylophone   | Pool B | 2101-03-01 | 2101-03-02 |
      | 1        | Tripod      | Pool B | 2101-02-01 | 2101-02-02 |
    When I log in as the user
    And I navigate to the cart
    Then I see the following lines in the "Items" section:
      | title          | body   | foot                 |
      | 1× DSLR Camera | Pool A | 2 days from 01/02/01 |
      | 1× DSLR Camera | Pool B | 2 days from 01/02/01 |
      | 1× Tripod      | Pool B | 2 days from 01/02/01 |
      | 1× DSLR Camera | Pool B | 2 days from 01/03/01 |
      | 1× Xylophone   | Pool B | 2 days from 01/03/01 |
      | 1× DSLR Camera | Pool A | 2 days from 01/04/01 |

  Scenario: Locale-/Language-based Formatting

    Text is shown in the chosen language,
    and dates are formatted according to the chosen locale.

    Given the following reservations exist for the user:
      | quantity | model       | pool   | start-date | end-date   |
      | 1        | DSLR Camera | Pool A | 2101-02-13 | 2101-02-13 |
      | 1        | Tripod      | Pool B | 2101-02-13 | 2101-02-14 |
    And I log in as the user

    # FIXME: us formatting is broken???
    # When I set my language to "en-US"
    # And I navigate to the cart
    # And I pry
    # Then I see the following lines in the "Items" section:
    #   | title          | body   | foot                 |
    #   | 1× DSLR Camera | Pool A | 1 day from 02/13/01  |
    #   | 1× Tripod      | Pool B | 2 days from 02/13/01 |

    When I set my language to "en-GB"
    And I navigate to the cart
    Then I see the following lines in the "Items" section:
      | title          | body   | foot                 |
      | 1× DSLR Camera | Pool A | 1 day from 13/02/01  |
      | 1× Tripod      | Pool B | 2 days from 13/02/01 |

    When I set my language to "de-CH"
    And I navigate to the cart
    Then I see the following lines in the "Gegenstände" section:
      | title          | body   | foot              |
      | 1× DSLR Camera | Pool A | 1 Tag ab 13.2.01  |
      | 1× Tripod      | Pool B | 2 Tage ab 13.2.01 |
