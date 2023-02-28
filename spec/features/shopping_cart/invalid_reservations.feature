Feature: Invalid reservations

  Background:
    Given a user with some mostly invalid reservations

  Scenario: Identity and fix invalid reservations

    Summary: When I have an invalid reservation in my cart, I must be able to identify the problem,
    also I must be able to fix it by either editing or removing the invalid reservation.
    This scenario tests through all typical constraint violations.

    When I log in as the user
    And I navigate to the cart
    And I see "29 minutes left"
    Then I see the following lines in the "Items" section:
      | title                       |
      | 1× Start Date In Past       |
      | 1× User is Suspended        |
      | 1× No Access To Pool        |
      | 1× Maximum Reservation Time |
      | 1× Model With No Items      |
      | 1× OK and Not Timed Out     |
      | 1× OK and Timed Out         |
      | 2× Quantity Too High        |
      | 1× Not A Workday            |
      | 1× Reservation Advance Days |
      | 1× Max Visits Count Pickup  |
      | 1× Holiday on End Date      |
      | 1× Max Visits Count Return  |

    And I see the text:
      """
      invalid items
      """


    # Now I click on each item. Not in order of appearance, but in order of "violation topic"
    # (bad start date | insufficient availability | can not visit | issue with pool | no violation)


    When I click on the card with title "1× Start Date In Past"
    Then I see the "Edit reservation" dialog
    But I see the following warnings in the "Time span" section:
      | text                       |
      | Pickup date is in the past |
    And I click on "Confirm"
    But the "Edit reservation" dialog did not close

    When I enter "${now}" in the "From" field
    And I enter "${1.day.from_now}" in the "Until" field
    And I click on "Confirm"
    Then the "Edit reservation" dialog has closed


    When I click on the card with title "1× Reservation Advance Days"
    Then I see the "Edit reservation" dialog
    But I see the following warnings in the "Time span" section:
      | text                                    |
      | Earliest pickup date in 3 days from now |
    And I click on "Confirm"
    But the "Edit reservation" dialog did not close

    When I enter "${3.days.from_now}" in the "From" field
    And I enter "${5.days.from_now}" in the "Until" field
    And I click on "Confirm"
    Then the "Edit reservation" dialog has closed


    When I click on the card with title "2× Quantity Too High"
    Then I see the "Edit reservation" dialog
    But I see the following warnings in the "Time span" section:
      | text                                                             |
      | Item is not available in the desired quantity during this period |
    And I click on "Confirm"
    But the "Edit reservation" dialog did not close

    When I click on "Minus 1"
    And I click on "Confirm"
    Then the "Edit reservation" dialog has closed


    When I click on the card with title "1× Not A Workday"
    Then I see the "Edit reservation" dialog
    But I see the following warnings in the "Time span" section:
      | text                                      |
      | Pickup not possible on ${now}             |
      | Return not possible on ${1.days.from_now} |
    And I click on "Confirm"
    But the "Edit reservation" dialog did not close

    # IMPROVE: the sample pool should not be closed on every day, so I don't have to delete the reservation
    When I click on "Remove reservation"
    Then the "Edit reservation" dialog has closed


    When I click on the card with title "1× Holiday on End Date"
    Then I see the "Edit reservation" dialog
    But I see the following warnings in the "Time span" section:
      | text                                      |
      | Return not possible on ${8.days.from_now} |
    And I click on "Confirm"
    But the "Edit reservation" dialog did not close

    And I enter "${9.days.from_now}" in the "Until" field
    And I click on "Confirm"
    Then the "Edit reservation" dialog has closed


    When I click on the card with title "1× Max Visits Count Pickup"
    Then I see the "Edit reservation" dialog
    But I see the following warnings in the "Time span" section:
      | text                                                                         |
      | Pickup not possible on ${4.days.from_now} (maximum visitor capacity reached) |
    And I click on "Confirm"
    But the "Edit reservation" dialog did not close

    And I enter "${3.days.from_now}" in the "From" field
    And I click on "Confirm"
    Then the "Edit reservation" dialog has closed


    When I click on the card with title "1× Max Visits Count Return"
    Then I see the "Edit reservation" dialog
    But I see the following warnings in the "Time span" section:
      | text                                                                          |
      | Return not possible on ${12.days.from_now} (maximum visitor capacity reached) |
    And I click on "Confirm"
    But the "Edit reservation" dialog did not close

    And I enter "${11.days.from_now}" in the "Until" field
    And I click on "Confirm"
    Then the "Edit reservation" dialog has closed


    When I click on the card with title "1× Maximum Reservation Time"
    Then I see the "Edit reservation" dialog
    But I see the following warnings in the "Time span" section:
      | text                                             |
      | Maximum reservation time is restricted to 7 days |
    And I click on "Confirm"
    But the "Edit reservation" dialog did not close

    And I enter "${6.days.from_now}" in the "Until" field
    And I click on "Confirm"
    Then the "Edit reservation" dialog has closed


    When I click on the card with title "1× Model With No Items"
    Then I see the "Edit reservation" dialog
    But I see the following warnings in the "Inventory pool" section:
      | text                                      |
      | Item not available in this inventory pool |
    And I click on "Confirm"
    But the "Edit reservation" dialog did not close

    When I click on "Remove reservation"
    Then the "Edit reservation" dialog has closed


    When I click on the card with title "1× No Access To Pool"
    Then I see the "Edit reservation" dialog
    But I see the following warnings in the "Inventory pool" section:
      | text                             |
      | No access to this inventory pool |
    And I click on "Confirm"
    But the "Edit reservation" dialog did not close

    When I click on "Remove reservation"
    Then the "Edit reservation" dialog has closed


    When I click on the card with title "1× User is Suspended"
    Then I see the "Edit reservation" dialog
    But I see the following warnings in the "Inventory pool" section:
      | text                                   |
      | User suspended for this inventory pool |
    And I click on "Confirm"
    But the "Edit reservation" dialog did not close

    When I click on "Remove reservation"
    Then the "Edit reservation" dialog has closed


    When I click on the card with title "1× OK and Timed Out"
    And I see the "Edit reservation" dialog
    And I click on "Confirm"
    Then the "Edit reservation" dialog has closed


    When I click on the card with title "1× OK and Not Timed Out"
    And I see the "Edit reservation" dialog
    And I click on "Confirm"
    Then the "Edit reservation" dialog has closed


    Then I see the following lines in the "Items" section:
      | title                       |
      | 1× Maximum Reservation Time |
      | 1× OK and Not Timed Out     |
      | 1× OK and Timed Out         |
      | 1× Quantity Too High        |
      | 1× Start Date In Past       |
      | 1× Reservation Advance Days |
      | 1× Max Visits Count Pickup  |
      | 1× Holiday on End Date      |
      | 1× Max Visits Count Return  |

    When I click on "Send order"
    Then I see the "Send order" dialog
