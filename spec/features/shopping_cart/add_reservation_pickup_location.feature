Feature: Shopping Cart - Adding a reservation with an alternative pickup location

    When a pool offers alternative pickup locations, the order dialog of a model lets the
    user choose one instead of the pool's main warehouse. The choice changes what the
    booking calendar offers, because items have to be transferred to and from that
    location, which costs the pool a buffer of order-processing days.

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And the inventory pool "Pool A" has the following details:
      | alternative pickup locations   | enabled |
      | transfer buffer before pick up | 0       |
      | transfer buffer after drop off | 0       |
    And the inventory pool "Pool A" has a pickup location "Media Lab"
    And the inventory pool "Pool A" has a pickup location "Studio Basement"
    And there is a model "DSLR Camera"
    And the following items exist:
      | code | model       | pool   |
      | A11  | DSLR Camera | Pool A |
      | A12  | DSLR Camera | Pool A |

  Scenario: Choosing an alternative pickup location

    Summary: The main warehouse is preselected. Picking a location instead puts it on the
    reservation, and the cart line shows it in place of the pool name.

    When I log in as the user
    And I visit the show page of model "DSLR Camera"
    And I click on "Add item"
    And I see the "DSLR Camera" dialog
    And I see a form inside the dialog
    And I see the "Pickup location" section
    Then the form has exactly these fields:
      | label                         | value            |
      | Inventory pool                | Pool A (max. 2)  |
      | Pickup location               | Hauptlager       |
      | Quantity                      | 1                |
      | From                          | ${Date.today}    |
      | Until                         | ${Date.tomorrow} |
      | Show availability in calendar | on               |
    And the "Pickup location" select offers these options:
      | option          |
      | Hauptlager      |
      | Media Lab       |
      | Studio Basement |

    When I select "Media Lab" from "Pickup location"
    And I click on "Add"
    Then the "DSLR Camera" dialog has closed
    And I accept the "Item added" dialog

    When I navigate to the cart
    And I sleep "0.5"
    Then I see the following lines in the "Items" section:
      | title          | body                                                                      |
      | 1× DSLR Camera | Media Lab\n${format_date_range_short(Date.today, Date.tomorrow)} (2 days) |
    And the newly created reservation in the DB has the pickup location "Media Lab"

  Scenario: Keeping the main warehouse

    Summary: Confirming without touching the select creates a reservation without a pickup
    location, and the cart line falls back to the pool name.

    When I log in as the user
    And I visit the show page of model "DSLR Camera"
    And I click on "Add item"
    And I see the "DSLR Camera" dialog
    And I see the "Pickup location" section
    And I click on "Add"
    Then the "DSLR Camera" dialog has closed
    And I accept the "Item added" dialog

    When I navigate to the cart
    And I sleep "0.5"
    Then I see the following lines in the "Items" section:
      | title          | body                                                                   |
      | 1× DSLR Camera | Pool A\n${format_date_range_short(Date.today, Date.tomorrow)} (2 days) |
    And the newly created reservation in the DB has no pickup location

  Scenario: The model is not transportable

    Summary: Such items can only be picked up at the main warehouse, so there is nothing to
    choose and the select is not rendered at all - only an explanation why.

    Given the model "DSLR Camera" is not transportable
    When I log in as the user
    And I visit the show page of model "DSLR Camera"
    And I click on "Add item"
    And I see the "DSLR Camera" dialog
    And I see a form inside the dialog
    Then the form has exactly these fields:
      | label                         | value            |
      | Inventory pool                | Pool A (max. 2)  |
      | Quantity                      | 1                |
      | From                          | ${Date.today}    |
      | Until                         | ${Date.tomorrow} |
      | Show availability in calendar | on               |
    And I see "Pickup and return only possible at the main location of the inventory pool."

  Scenario: Alternative pickup locations are disabled for the pool

    Summary: Neither the select nor the explanation is shown - the feature is invisible.

    Given the inventory pool "Pool A" has the following details:
      | alternative pickup locations | disabled |
    When I log in as the user
    And I visit the show page of model "DSLR Camera"
    And I click on "Add item"
    And I see the "DSLR Camera" dialog
    And I see a form inside the dialog
    Then the form has exactly these fields:
      | label                         | value            |
      | Inventory pool                | Pool A (max. 2)  |
      | Quantity                      | 1                |
      | From                          | ${Date.today}    |
      | Until                         | ${Date.tomorrow} |
      | Show availability in calendar | on               |
    And I don't see "Pickup and return only possible at the main location of the inventory pool."

  Scenario: More details about the pickup locations

    Summary: The link next to the select leads to the pool page, which describes the
    locations.

    When I log in as the user
    And I visit the show page of model "DSLR Camera"
    And I click on "Add item"
    And I see the "DSLR Camera" dialog
    And I see the "Pickup location" section
    Then the "Pickup location" section links "More details about the alternative pickup locations" to the page of pool "Pool A"

  Scenario: Switching the inventory pool

    Summary: A pickup location belongs to exactly one pool, so switching the pool always
    drops the selection back to the main warehouse and offers the new pool's locations.

    Given there is an inventory pool "Pool B"
    And the user is customer of pool "Pool B"
    And the inventory pool "Pool B" has the following details:
      | alternative pickup locations   | enabled |
      | transfer buffer before pick up | 0       |
      | transfer buffer after drop off | 0       |
    And the inventory pool "Pool B" has a pickup location "Annex"
    And the following items exist:
      | code | model       | pool   |
      | B11  | DSLR Camera | Pool B |

    When I log in as the user
    And I visit the show page of model "DSLR Camera"
    And I click on "Add item"
    And I see the "DSLR Camera" dialog
    And I see a form inside the dialog
    And I see the "Pickup location" section
    And I select "Media Lab" from "Pickup location"

    When I select "Pool B (max. 1)" from "Inventory pool"
    And I see the "Pickup location" section
    Then the "Pickup location" select offers these options:
      | option     |
      | Hauptlager |
      | Annex      |
    And the "Pickup location" select shows "Hauptlager"

    When I select "Annex" from "Pickup location"
    And I click on "Add"
    Then the "DSLR Camera" dialog has closed
    And I accept the "Item added" dialog
    And the newly created reservation in the DB has the pickup location "Annex"

  Scenario: The transfer buffer moves the earliest possible pickup date

    Summary: Without a pickup location the reservation advance days apply, with one the
    transfer buffer does - whichever of the two is larger. Toggling the select re-reads the
    booking calendar.

    Given the inventory pool "Pool A" has the following details:
      | reservation advance days       | 1 |
      | transfer buffer before pick up | 3 |

    When I log in as the user
    And I visit the show page of model "DSLR Camera"
    And I click on "Add item"
    And I see the "DSLR Camera" dialog
    And I see a form inside the dialog
    And I see the "Pickup location" section
    And I enter the date "${Date.tomorrow}" in the "From" field
    And I enter the date "${4.days.from_now}" in the "Until" field
    And I press the tab key
    Then I see no warnings in the "Time span" section

    When I select "Media Lab" from "Pickup location"
    Then I see the following warnings in the "Time span" section:
      | text                                            |
      | Earliest pickup date in 3 working days from now |
    And the "Add" button of the dialog is disabled

    When I select "Hauptlager" from "Pickup location"
    Then I see no warnings in the "Time span" section

    When I select "Media Lab" from "Pickup location"
    And I enter the date "${3.days.from_now}" in the "From" field
    And I press the tab key
    Then I see no warnings in the "Time span" section
    When I click on "Add"
    Then the "DSLR Camera" dialog has closed

  Scenario: The transfer buffer blocks the days around an existing transferred reservation

    Summary: A reservation that is itself picked up at an alternative location occupies the
    item for the buffer days before and after it, but only for other transferred bookings -
    a pickup at the main warehouse is unaffected.

    Given the inventory pool "Pool A" has the following details:
      | transfer buffer before pick up | 2 |
      | transfer buffer after drop off | 2 |
    And there is another user "Bob Blocker"
    And the following reservations exist for the user "Bob Blocker":
      | quantity | model       | pool   | pickup-location | relative-start-date | relative-end-date   | status   |
      | 2        | DSLR Camera | Pool A | Studio Basement | ${10.days.from_now} | ${11.days.from_now} | approved |

    When I log in as the user
    And I visit the show page of model "DSLR Camera"
    And I click on "Add item"
    And I see the "DSLR Camera" dialog
    And I see a form inside the dialog
    And I see the "Pickup location" section

    # Bob is picked up in 10 days and dropped off in 11, so with the buffers his
    # reservation occupies the item from day 8 to day 13. Far enough away, a transferred
    # pickup of our own - which needs the same gap around itself, so it reaches from day 3
    # to day 7 - still fits.
    When I enter the date "${5.days.from_now}" in the "From" field
    And I enter the date "${5.days.from_now}" in the "Until" field
    And I press the tab key
    Then I see no warnings in the "Time span" section
    When I select "Media Lab" from "Pickup location"
    Then I see no warnings in the "Time span" section

    # Two days later our own buffer would reach into Bob's - but only if we have the item
    # transferred. Picking it up at the main warehouse needs no gap at all.
    When I select "Hauptlager" from "Pickup location"
    And I enter the date "${7.days.from_now}" in the "From" field
    And I enter the date "${7.days.from_now}" in the "Until" field
    And I press the tab key
    Then I see no warnings in the "Time span" section

    When I select "Media Lab" from "Pickup location"
    Then I see the following warnings in the "Time span" section:
      | text                                                                   |
      | Item is not available in the requested quantity on ${7.days.from_now}. |
    And the "Add" button of the dialog is disabled
