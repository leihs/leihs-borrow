Feature: Rentals - Show - Repeat order

  Background:
    Given there is an initial admin

    And there is an inventory pool "Pool A"
    And there is an inventory pool "Pool B"

    And there is a model "DSLR Camera"
    And there is a model "Tripod"
    And there is a model "Elefant"
    And the following items exist:
      | code | model       | pool   |
      | A1   | DSLR Camera | Pool A |
      | A2   | Tripod      | Pool A |
      | B1   | Elefant     | Pool B |

    And there is a user "User A"
    And there is a delegation "Delegation D"
    And the user is member of delegation "Delegation D"

    And the user is customer of pool "Pool A"
    And the delegation is customer of pool "Pool A"

  Scenario Outline: Repeating an order
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | user                  | quantity | model       | pool   | start-date | end-date   | state  |
      | <sourceProfileLookup> | 1        | DSLR Camera | Pool A | 2020-02-01 | 2020-02-10 | closed |
      | <sourceProfileLookup> | 1        | Tripod      | Pool A | 2020-02-05 | 2020-02-05 | closed |
      | <sourceProfileLookup> | 1        | Elefant     | Pool B | 2020-03-01 | 2020-03-01 | closed |

    When I log in as the user

    # switch to source profile
    When I click on the profile button
    And I click on "<sourceProfile>"
    Then the profile button shows "<sourceProfileShort>"

    And I visit "/app/borrow/rentals/"
    And I click on "Order 1"

    Then I see the page title "Order 1"
    And I see the following status rows in the "State" section:
      | title              | progressbar | info |
      | All items returned |             |      |

    # switch to source profile
    When I click on the profile button
    And I click on "<targetProfile>"
    Then the profile button shows "<targetProfileShort>"

    When I click on "Repeat order"
    Then I see the "Add items" dialog
    And I see "3 items will be added to the cart."
    And I see the following text in the dialog:
      """
      Order for
      <targetProfile>
      """
    And I see the date "${Date.today}" in the "From" field
    And I see the date "${Date.tomorrow}" in the "Until" field

    When I enter the date "${3.day.from_now}" in the "From" field
    And I enter the date "${4.day.from_now}" in the "Until" field
    And I accept the "Add items" dialog
    Then the "Add items" dialog has closed
    And I see the "Items added" dialog with the text:
      """
      3 items were added to the cart and can be reviewed/edited there.
      """

    When I click on "Go to cart"
    Then the "Items added" dialog has closed
    And I see the page title "Cart"
    And I see the following lines in the "Items" section:
      | title          | body   | foot                          |
      | 1× DSLR Camera | Pool A | 2 days from ${3.day.from_now} |
      | 1× Tripod      | Pool A | 2 days from ${3.day.from_now} |
      | 1× Elefant     | Pool B | 2 days from ${3.day.from_now} |
    And I see the text:
      """
      1 invalid item
      """

    And I click on the card with title "1× Elefant"
    And I see the "Edit reservation" dialog
    And I click on "Remove reservation"
    And the "Edit reservation" dialog has closed
    And I sleep "0.5"
    And I click on "Send order"
    And I enter "Order 1 (again)" in the "Title" field
    And I accept the "Send order" dialog
    And the "Send order" dialog has closed
    And I accept the "Order submitted" dialog
    And the "Order submitted" dialog has closed
    And I see the page title "My Orders"

    Examples:
      | sourceProfileLookup | sourceProfile | sourceProfileShort | targetProfile | targetProfileShort |
      | user                | User A        | UA                 | User A        | UA                 |
      | Delegation D        | Delegation D  | DD                 | Delegation D  | DD                 |
      | user                | User A        | UA                 | Delegation D  | DD                 |
      | Delegation D        | Delegation D  | DD                 | User A        | UA                 |

  Scenario: Order with options only
    Given there is an option "Ethernet 1.5m"
    And there is an option "USB Adapter"
    And a customer order with title "Order 1" and the following reservations exists for the user:
      | user | quantity | option        | pool   | start-date | end-date   | state  |
      | user | 1        | Ethernet 1.5m | Pool A | 2020-02-01 | 2020-02-10 | closed |
      | user | 1        | USB Adapter   | Pool A | 2020-02-05 | 2020-02-05 | closed |

    When I log in as the user
    And I visit "/app/borrow/rentals/"
    And I click on "Order 1"
    And I see the page title "Order 1"
    And I click on "Repeat order"

    Then I see the "Add items" dialog
    And I see a warning in the dialog:
      """
      Options can only be added by the lending desk.
      """
    And the "Add" button is disabled

  Scenario: Order with models and an option
    Given there is an option "USB Adapter"
    And a customer order with title "Order 1" and the following reservations exists for the user:
      | user | quantity | model       | option      | pool   | start-date | end-date   | state  |
      | user | 1        | DSLR Camera |             | Pool A | 2020-02-01 | 2020-02-10 | closed |
      | user | 1        | Tripod      |             | Pool A | 2020-02-01 | 2020-02-10 | closed |
      | user | 1        |             | USB Adapter | Pool A | 2020-02-05 | 2020-02-05 | closed |

    When I log in as the user
    And I visit "/app/borrow/rentals/"
    And I click on "Order 1"
    And I see the page title "Order 1"
    And I click on "Repeat order"

    Then I see the "Add items" dialog
    And I see "2 items will be added to the cart."
    And I see a warning in the dialog:
      """
      Please note: One option can only be added by the lending desk.
      """

    When I accept the "Add items" dialog
    And the "Add items" dialog has closed

    Then I see the "Items added" dialog with the text:
      """
      2 items were added to the cart and can be reviewed/edited there.
      """

    When I click on "Go to cart"
    And the "Items added" dialog has closed

    Then I see the page title "Cart"
    And I see the following lines in the "Items" section:
      | title          | body   | foot                      |
      | 1× DSLR Camera | Pool A | 2 days from ${Date.today} |
      | 1× Tripod      | Pool A | 2 days from ${Date.today} |
