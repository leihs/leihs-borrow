Feature: Invalid reservations

  Background:
    Given a user with some mostly invalid reservations

  Scenario: Identity and fix invalid reservations

    Summary: When I have an invalid reservation in my cart, I must be able to identify the problem,
    also I must be able to fix it by either editing or removing the invalid reservation.
    This scenario tests through all typical constraint violations.

    When I log in as the user
    And I navigate to the cart
    And I click on "Reset time limit"
    And I see "30 minutes left"
    Then I see the following lines in the "Items" section:
      | title                           |
      | 1× Start Date In Past           |
      | 1× User is Suspended            |
      | 1× No Access To Pool            |
      | 1× Maximum Reservation Duration |
      | 1× Model With No Items          |
      | 1× OK and Not Timed Out         |
      | 1× OK and Timed Out             |
      | 2× Quantity Too High            |
      | 1× Not A Workday                |
      | 1× Reservation Advance Days     |
      | 1× Max Visits Count Pickup      |
      | 1× Holiday on End Date          |
      | 1× Max Visits Count Return      |

    And I see the text:
      """
      invalid items
      """


    # Now I click on each item. Not in order of appearance, but in order of "violation topic"
    # (bad start date | insufficient availability | can not visit | issue with pool | no violation)


    When I click on the card with title "1× Start Date In Past"
    Then I see the "Start Date In Past" dialog
    But I see the following warnings in the "Time span" section:
      | text                       |
      | Pickup date is in the past |
    And I click on "Confirm"
    But the "Start Date In Past" dialog did not close

    When I enter "${now}" in the "From" field
    And I enter "${1.day.from_now}" in the "Until" field
    And I click on "Confirm"
    Then the "Start Date In Past" dialog has closed


    When I click on the card with title "1× Reservation Advance Days"
    Then I see the "Reservation Advance Days" dialog
    But I see the following warnings in the "Time span" section:
      | text                                            |
      | Earliest pickup date in 3 working days from now |
    And I click on "Confirm"
    But the "Reservation Advance Days" dialog did not close

    When I enter "${3.days.from_now}" in the "From" field
    And I enter "${5.days.from_now}" in the "Until" field
    And I click on "Confirm"
    Then the "Reservation Advance Days" dialog has closed


    When I click on the card with title "2× Quantity Too High"
    Then I see the "Quantity Too High" dialog
    But I see the following warnings in the "Time span" section:
      | text                                                             |
      | Item is not available in the desired quantity during this period |
    And I click on "Confirm"
    But the "Quantity Too High" dialog did not close

    When I click on "Minus 1"
    And I click on "Confirm"
    Then the "Quantity Too High" dialog has closed


    When I click on the card with title "1× Not A Workday"
    Then I see the "Not A Workday" dialog
    But I see the following warnings in the "Time span" section:
      | text                                                                                    |
      | Pickup not possible on ${now} (closed on ${now.strftime('%A')})                         |
      | Return not possible on ${1.days.from_now} (closed on ${1.days.from_now.strftime('%A')}) |
    And I click on "Confirm"
    But the "Not A Workday" dialog did not close

    # IMPROVE: the sample pool should not be closed on every day, so I don't have to delete the reservation
    When I click on "Remove reservation"
    Then the "Not A Workday" dialog has closed


    When I click on the card with title "1× Holiday on End Date"
    Then I see the "Holiday on End Date" dialog
    But I see the following warnings in the "Time span" section:
      | text                                               |
      | Return not possible on ${8.days.from_now} (Ogtern) |
    And I click on "Confirm"
    But the "Holiday on End Date" dialog did not close

    And I enter "${9.days.from_now}" in the "Until" field
    And I click on "Confirm"
    Then the "Holiday on End Date" dialog has closed


    When I click on the card with title "1× Max Visits Count Pickup"
    Then I see the "Max Visits Count Pickup" dialog
    But I see the following warnings in the "Time span" section:
      | text                                                                         |
      | Pickup not possible on ${4.days.from_now} (maximum visitor capacity reached) |
    And I click on "Confirm"
    But the "Max Visits Count Pickup" dialog did not close

    And I enter "${3.days.from_now}" in the "From" field
    And I click on "Confirm"
    Then the "Max Visits Count Pickup" dialog has closed


    When I click on the card with title "1× Max Visits Count Return"
    Then I see the "Max Visits Count Return" dialog
    But I see the following warnings in the "Time span" section:
      | text                                                                          |
      | Return not possible on ${12.days.from_now} (maximum visitor capacity reached) |
    And I click on "Confirm"
    But the "Max Visits Count Return" dialog did not close

    And I enter "${11.days.from_now}" in the "Until" field
    And I click on "Confirm"
    Then the "Max Visits Count Return" dialog has closed


    When I click on the card with title "1× Maximum Reservation Duration"
    Then I see the "Maximum Reservation Duration" dialog
    But I see the following warnings in the "Time span" section:
      | text                                                 |
      | Maximum reservation duration is restricted to 7 days |
    And I click on "Confirm"
    But the "Maximum Reservation Duration" dialog did not close

    And I enter "${6.days.from_now}" in the "Until" field
    And I click on "Confirm"
    Then the "Maximum Reservation Duration" dialog has closed


    When I click on the card with title "1× Model With No Items"
    Then I see the "Model With No Items" dialog
    But I see the following warnings in the "Inventory pool" section:
      | text                                      |
      | Item not available in this inventory pool |
    And I click on "Confirm"
    But the "Model With No Items" dialog did not close

    When I click on "Remove reservation"
    Then the "Model With No Items" dialog has closed


    When I click on the card with title "1× No Access To Pool"
    Then I see the "No Access To Pool" dialog
    But I see the following warnings in the "Inventory pool" section:
      | text                             |
      | No access to this inventory pool |
    And I click on "Confirm"
    But the "No Access To Pool" dialog did not close

    When I click on "Remove reservation"
    Then the "No Access To Pool" dialog has closed


    When I click on the card with title "1× User is Suspended"
    Then I see the "User is Suspended" dialog
    But I see the following warnings in the "Inventory pool" section:
      | text                                   |
      | User suspended for this inventory pool |
    And I click on "Confirm"
    But the "User is Suspended" dialog did not close

    When I click on "Remove reservation"
    Then the "User is Suspended" dialog has closed


    When I click on the card with title "1× OK and Timed Out"
    And I see the "OK and Timed Out" dialog
    And I click on "Confirm"
    Then the "OK and Timed Out" dialog has closed


    When I click on the card with title "1× OK and Not Timed Out"
    And I see the "OK and Not Timed Out" dialog
    And I click on "Confirm"
    Then the "OK and Not Timed Out" dialog has closed


    Then I see the following lines in the "Items" section:
      | title                           |
      | 1× Maximum Reservation Duration |
      | 1× OK and Not Timed Out         |
      | 1× OK and Timed Out             |
      | 1× Quantity Too High            |
      | 1× Start Date In Past           |
      | 1× Reservation Advance Days     |
      | 1× Max Visits Count Pickup      |
      | 1× Holiday on End Date          |
      | 1× Max Visits Count Return      |

    When I click on "Send order"
    Then I see the "Send order" dialog

  Scenario: Alternative pickup location invalid cart cases

    Given a user with invalid alternative pickup reservations
    When I log in as the user
    And I navigate to the cart
    And I click on "Reset time limit"
    Then I see the following lines in the "Items" section:
      | title                            |
      | 1× Non Transportable Alt         |
      | 1× Transfer Buffer Before Pickup |

    When I click on the card with title "1× Non Transportable Alt"
    Then I see the "Non Transportable Alt" dialog
    And I see the following warnings in the "Time span" section:
      | text                                                              |
      | The previously selected pickup location is not available for this item. |
    And I click on "Cancel"
    Then the "Non Transportable Alt" dialog has closed

    When I click on the card with title "1× Transfer Buffer Before Pickup"
    Then I see the "Transfer Buffer Before Pickup" dialog
    But I see the following warnings in the "Time span" section:
      | text                                            |
      | Earliest pickup date in 3 working days from now |
    And I click on "Cancel"
    Then the "Transfer Buffer Before Pickup" dialog has closed
