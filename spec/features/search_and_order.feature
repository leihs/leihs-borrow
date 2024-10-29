Feature: Search and order

  Background:
    Given there is an initial admin
    And there is a delegation "Delegation D"
    And there is a user "User A"
    And the user is member of delegation "Delegation D"
    And there is an inventory pool "Pool A"
    And the user is inventory manager of pool "Pool A"
    And there is an inventory pool "Pool B"
    And the user is inventory manager of pool "Pool B"
    And the delegation "Delegation D" is customer of pool "Pool A"
    And the delegation "Delegation D" is customer of pool "Pool B"

    And the receival of received order emails is activated for all pools

  Scenario Outline: Order for delegation <name>
    Given there is a model "Kamera"
    And there is 1 borrowable item for model "Kamera" in pool "Pool A"
    And there are 2 borrowable items for model "Kamera" in pool "Pool B"
    And I log in as the user

    # switch to delegation profile
    When I click on the user profile button
    And I select "<name>" from "Switch Profile"
    Then the user profile button shows "<shortname>"

    # search for a model
    And I visit "/borrow/"
    And I enter "Kamera" in the "Search term" field
    And I select "Pool B" from "Inventory pool"
    And I click on "availability"
    And I enter the date "${Date.tomorrow}" in the "From" field
    And I enter the date "${Date.tomorrow + 1.day}" in the "Until" field
    And I enter quantity "2"
    And I click button "Apply"
    Then I see one model with the title "Kamera"

    # make a reservation
    When I click on the model with the title "Kamera"
    Then the show page of the model "Kamera" was loaded
    And I click on "Add item"
    Then the order panel is shown
    And the pools select box shows "Pool B"
    And the start date has "${Date.tomorrow}"
    And the end date has "${Date.tomorrow + 1.day}"
    And the quantity has "2"
    And I enter quantity "1"
    And I click on "Add"
    And the "Add item" dialog has closed
    And I accept the "Item added" dialog with the text:
      """
      The item was added to the cart
      """
    And the "Item added" dialog has closed

    # check the cart
    When I click on the cart icon
    Then I see the following lines in the "Items" section:
      | title     | body   |
      | 1× Kamera | Pool B |

    # submit the order
    When I click on "Send order"
    And I name the order as "Order 1"
    And I click on "Send"
    And the "Send order" dialog has closed
    And I accept the "Order submitted" dialog
    And the "Order submitted" dialog has closed

    # emails
    And there have been 2 emails created
    And a submitted email has been created for user "User A"
    And a received email has been created for pool "Pool B"

    # approve the order
    Then I approve the order "Order 1"

    # check the new status of the order
    When I visit "/borrow/"
    # FIXME: wait for menu open
    And I sleep 1
    And I click on "Orders"
    Then I see the order "Order 1" under open orders

    # check the content of the order
    When I click on the card with title "Order 1"
    Then I see "1× Kamera"

    Examples:
      | name              | shortname |
      | Delegation D      | DD        |
      | User A (personal) | UA        |

  Scenario: Overbooking of expired reservation 

    The user has a reservation for a particular model in the cart which is expired.
    If he adds a new reservation for the same model in the cart intersecting date-wise
    the expired reservation, then the cart marks both reservations as invalid. If he
    does the same for the 3rd reservation, then both previous reservations are still
    marked as invalid but the newest 3rd one is ok.

    Given the cart timeout is set to 1 minute
    And there is a model "Kamera"
    And there is 1 borrowable item for model "Kamera" in pool "Pool A"
    And I log in as the user

    # make the 1st reservation
    When I visit "/borrow/"
    And I enter "Kamera" in the "Search term" field
    And I click on "Search"
    And I click on the model with the title "Kamera"
    Then the show page of the model "Kamera" was loaded
    And I click on "Add item"
    Then the order panel is shown
    And the pools select box shows "Pool A"
    And I set "${Date.today}" as the start date
    And I set "${Date.today}" as the end date
    And I click on "Add"
    And the "Add item" dialog has closed
    And I accept the "Item added" dialog with the text:
    """
    The item was added to the cart
    """
    And the "Item added" dialog has closed

    # check the cart
    When I click on the cart icon
    Then I see the following lines in the "Items" section:
      | title     | body   |
      | 1× Kamera | Pool A |

    And I wait for 61 seconds
    And the cart is expired

    # make the 2nd reservation
    When I visit "/borrow/"
    And I enter "Kamera" in the "Search term" field
    And I click on "Search"
    And I click on the model with the title "Kamera"
    Then the show page of the model "Kamera" was loaded
    And I click on "Add item"
    Then the order panel is shown
    And the pools select box shows "Pool A"
    And I set "${Date.today}" as the start date
    And I set "${Date.tomorrow}" as the end date
    And I click on "Add"
    And the "Add item" dialog has closed
    And I accept the "Item added" dialog with the text:
    """
    The item was added to the cart
    """
    And the "Item added" dialog has closed
   
    # check the cart
    When I click on the cart icon
    Then I see the following lines in the Items section:
      | title     | pool   | start_date    | duration | valid | 
      | 1× Kamera | Pool A | ${Date.today} | 1        | false |
      | 1× Kamera | Pool A | ${Date.today} | 2        | false |
    And the "Send order" button is disabled
