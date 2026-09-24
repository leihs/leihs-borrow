Feature: Shopping Cart - Editing Reservations with an alternative pickup location
    A reservation in the cart can carry an alternative pickup location.
    When the edit dialog is opened, that location is preselected.
  If it is not available any more, the dialog says so instead of silently
  falling back to the main warehouse.

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And the inventory pool "Pool A" has the following details:
      | alternative pickup locations   | enabled |
      | transfer buffer before pick up | 0       |
    And the inventory pool "Pool A" has a pickup location "Media Lab"
    And the inventory pool "Pool A" has a pickup location "Studio Basement"
    And there is a model "DSLR Camera"
    And the following items exist:
      | code | model       | pool   |
      | A11  | DSLR Camera | Pool A |
      | A12  | DSLR Camera | Pool A |
    And the following reservations exist for the user:
      | quantity | model       | pool   | pickup-location | relative-start-date | relative-end-date  |
      | 1        | DSLR Camera | Pool A | Studio Basement | ${Date.today}       | ${2.days.from_now} |

  Scenario: The preselected pickup location is still available

    When I log in as the user
    And I navigate to the cart
    And I sleep "0.5"
    Then I see the following lines in the "Items" section:
      | title          | body                                                                              |
      | 1× DSLR Camera | Studio Basement\n${format_date_range_short(Date.today, 2.days.from_now)} (3 days) |

    When I click on the card with title "1× DSLR Camera"
    And I see the "DSLR Camera" dialog
    And I see a form inside the dialog
    # the pickup location only becomes known once the availability has been fetched
    And I see the "Pickup location" section
    Then the form has exactly these fields:
      | label                         | value              |
      | Inventory pool                | Pool A (max. 2)    |
      | Pickup location               | Studio Basement    |
      | Quantity                      | 1                  |
      | From                          | ${Date.today}      |
      | Until                         | ${2.days.from_now} |
      | Show availability in calendar | on                 |
    And the form has no error message

    When I click on "Confirm"
    Then the "DSLR Camera" dialog has closed
    And I sleep "0.5"
    And I see the following lines in the "Items" section:
      | title          | body                                                                              |
      | 1× DSLR Camera | Studio Basement\n${format_date_range_short(Date.today, 2.days.from_now)} (3 days) |

  Scenario: The pickup location is still enabled, but the model is not transportable

    Summary: There is nothing to choose, so the dialog only informs about the dropped
    selection and can still be confirmed - which moves the reservation to the main warehouse.

    Given the model "DSLR Camera" is not transportable
    When I log in as the user
    And I navigate to the cart
    And I sleep "0.5"

    When I click on the card with title "1× DSLR Camera"
    And I see the "DSLR Camera" dialog
    And I see a form inside the dialog
    # the pickup location only becomes known once the availability has been fetched
    And I see the "Pickup location" section
    Then the form has exactly these fields:
      | label                         | value              |
      | Inventory pool                | Pool A (max. 2)    |
      | Quantity                      | 1                  |
      | From                          | ${Date.today}      |
      | Until                         | ${2.days.from_now} |
      | Show availability in calendar | on                 |
    And I see the following warnings in the "Pickup location" section:
      | text                                                                    |
      | The previously selected pickup location is not available for this item. |

    When I click on "Confirm"
    Then the "DSLR Camera" dialog has closed
    And I sleep "0.5"
    And I see the following lines in the "Items" section:
      | title          | body                                                                     |
      | 1× DSLR Camera | Pool A\n${format_date_range_short(Date.today, 2.days.from_now)} (3 days) |

  Scenario: The selected pickup location has been deactivated

    Summary: The pool still offers other pickup locations, so the user has to pick one
    (or the main warehouse) before the dialog can be confirmed.

    Given the pickup location "Studio Basement" is deactivated
    When I log in as the user
    And I navigate to the cart
    And I sleep "0.5"

    When I click on the card with title "1× DSLR Camera"
    And I see the "DSLR Camera" dialog
    And I see a form inside the dialog
    # the pickup location only becomes known once the availability has been fetched
    And I see the "Pickup location" section
    Then the form has exactly these fields:
      | label                         | value              |
      | Inventory pool                | Pool A (max. 2)    |
      | Pickup location               | Hauptlager         |
      | Quantity                      | 1                  |
      | From                          | ${Date.today}      |
      | Until                         | ${2.days.from_now} |
      | Show availability in calendar | on                 |
    And I see the following warnings in the "Pickup location" section:
      | text                                                                    |
      | The previously selected pickup location is not available for this item. |

    When I click on "Confirm"
    Then the "DSLR Camera" dialog did not close

    When I select "Media Lab" from "Pickup location"
    Then the form has no error message
    When I click on "Confirm"
    Then the "DSLR Camera" dialog has closed
    And I sleep "0.5"
    And I see the following lines in the "Items" section:
      | title          | body                                                                        |
      | 1× DSLR Camera | Media Lab\n${format_date_range_short(Date.today, 2.days.from_now)} (3 days) |

  Scenario: Alternative pickup locations have been disabled for the pool

    Summary: Like the not transportable case there is nothing to choose,
    so the dialog can still be confirmed.

    Given the inventory pool "Pool A" has the following details:
      | alternative pickup locations | disabled |
    When I log in as the user
    And I navigate to the cart
    And I sleep "0.5"

    When I click on the card with title "1× DSLR Camera"
    And I see the "DSLR Camera" dialog
    And I see a form inside the dialog
    # the pickup location only becomes known once the availability has been fetched
    And I see the "Pickup location" section
    Then the form has exactly these fields:
      | label                         | value              |
      | Inventory pool                | Pool A (max. 2)    |
      | Quantity                      | 1                  |
      | From                          | ${Date.today}      |
      | Until                         | ${2.days.from_now} |
      | Show availability in calendar | on                 |
    And I see the following warnings in the "Pickup location" section:
      | text                                                                    |
      | The previously selected pickup location is not available for this item. |

    When I click on "Confirm"
    Then the "DSLR Camera" dialog has closed
    And I sleep "0.5"
    And I see the following lines in the "Items" section:
      | title          | body                                                                     |
      | 1× DSLR Camera | Pool A\n${format_date_range_short(Date.today, 2.days.from_now)} (3 days) |
