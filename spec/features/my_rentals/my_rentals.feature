Feature: Bla

    Background:

    # :IN_APPROVAL {:de-CH "Genehmigung" :en-GB "In Approval"}
    #                                    :TO_PICKUP {:de-CH "Abholung" :en-GB "To pick up"}
    #                                   :TO_RETURN {:de-CH "Rückgabe" :en-GB "To return"}
    #                                  :RETURNED {:de-CH "Alle Gegenstände zurückgebracht" :en-GB "All items returned"}
    #                                 :REJECTED {:de-CH "Ausleihe wurde abgelehnt" :en-GB "Rental was rejeced"}
    #                                :CANCELED {:de-CH "Ausleihe wurde storniert" :en-GB "Rental was canceled"}}
    #reservation = 1 item line
    #order = Gefäss mit mehreren item lines

    Background:
        Given there is an initial admin
        And there is a user
        And there is a delegation "Project Saalschutz"
        And there is an inventory pool "Pool A"
        And the user is customer of pool "Pool A"
        And there is an inventory pool "Pool B"
        And the user is customer of pool "Pool B"
        And there is a model "DSLR Camera"
        And there is a model "Tripod"
        And the following items exist:¦
            | code | model       | pool   |
            | A11  | DSLR Camera | Pool A |
            | A12  | DSLR Camera | Pool A |
            | A13  | DSLR Camera | Pool A |
            | A21  | Tripod      | Pool B |
        And the following orders exist
            | title        | Purpose      | model       | pool   | quantity | start-date | end-date   | status      | delegation         |
            | blue my mind | blue my mind | DSLR Camera | Pool A | 2        | 2032-02-01 | 2032-02-03 | In_Approval | Project Saalschutz |
            | blue my mind | blue my mind | Tripod      | Pool A | 1        | 2032-02-01 | 2032-02-04 | In_Approval | Project Saalschutz |
            | open box     | open box     | DSLR Camera | Pool A | 2        | 2021-01-01 | 2021-02-03 | Returned    | my user            |
            | open box     | open box     | Tripod      | Pool A | 1        | 2021-01-01 | 2021-02-03 | Returned    | my user            |

    Scenario: Filter
        When I log in as user
        And I am assigned to a delegation
        And I navigate to my rentals
        Then I see the filter is collapsed
        When I press on the filter
        Then the filter opens
        And I see a dropdown which contains my user and the delegation "Projekt Saalschutz"
        And my user is selected
        And I see a field to enter the search term
        And the field is empty
        And I see a dropdown to select a status
        And all status are selected
        And I see a date picker where I can select the start date
        And the start date is not limited
        And I see a date picker where I can select the end date
        And the end date is not limited
        And I see a drop down with pools listed where I have at least customer rights
        And all pools are selected
        When I klick "Anwenden"
        Then all rentals are shown for my user


    Scenario: Filter with selections
        When I log in as user
        And I am assigned to a delegation
        And I navigate to my rentals
        Then I see the filter is collapsed
        When I press on the filter
        Then the filter opens
        When I select the delegation "Project Saalschutz"
        And I enter the search term "Camera"
        And I select the status "In_Approval"
        And I choose the start date "2032-02-02"
        And I choose the pool "Pool A"
        And I klick on "Anwenden"
        Then no rentals are shown
        When I open the filter
        And I choose the start date "2032-01-21"
        And I klick on "Anwenden"
        Then I see the my order
        When I open the order
        And I klick on "Zurücksetzen"
        Then all rentals are shown for my user

    Scenario: Keep filter when navigating back from detail page
        When I log in as user
        And I navigate to my rentals
        Then I see the filter is collapsed
        When I press on the filter
        Then the filter opens
        When I select the delegation "Project Saalschutz"
        And I klick on "Anwenden"
        Then the rentals for the delegation "Project Saalschutz" are shown
        When I open the detail page of the rental
        And I navigate back to my rentals
        Then the delegation "Project Saalschutz" is still selected
        When I log out of leihs
        And I log in as user
        And I navigate to my rentals
        And I open the filter
        Then my user is selected

    Background:
        Given there is an initial admin
        And there is a user
        And the user is not assigned to a delegation

    Scenario: Filter - no delegation dropdown if not assigned to a delegation
        When I log in as user
        And I navigate to my rentals
        Then I see the filter is collapsed
        When I press on the filter
        Then the filter opens
        And my user is shown in the filter
        And I can not choose another user

    Scenario: What is shown in a not yet accepted order
        When I log in as the user
        And I navigate to my rentals
        Then I see my order with the title "blue my mind"
        And I see the maximum amount of days is 3
        And I see the start-date of my order is "2032-02-01"
        And I see the amount of items of this order is 3
        And I see the state of the order is "In_Approval"
        And the status bar shows "0%"
        And I see that 0 of 3 items have been accepted

    Scenario: Open order details of an order not yet accepted
        When I klick on the title "blue my mind"
        Then  I see the title "blue my mind"
        And I see  on the page with the title
        And I see the maximum amount of days is 3
        And I see the start-date of my order is "2032-02-01"
        And I see the amount of items of this order is 3
        And I see the state of the order is "In_Approval"
        And the status bar shows "0%"
        And I see that 0 of 3 items have been accepted
        And I see the pupose of the order is "blue my mind"
        And I see lending park is "Pool A"
        And the status bar shows "0%"
        And I see Pool A has not yet accepted any items
        And I see I have ordered 2 items of model "DSLR Camera" from "Pool A"
        And I see the lending time is 2 days
        And I see the lending start date is "2032-02-01"
        And I see I have ordered 1 item of model "Tripod" from "Pool A"
        And I see the lending time is 3 days
        And I see the lending start date is "2032-02-01"
        And I see that I have sent the order with my user

    Scenario: order from one pool
    Scenario: order from several pools
    Scenario: order for only one pool with no items accepted yet
    Scenario: order for only one pool with some items accepted
    Scenario: order for only one pool with all items accepted
    Scenario: order for several pools with no items accepted yet
    Scenario: order for several pools with some items accepted
    Scenario: order for several pools with all items accepted yet
    Scenario: order for one pool rejected
    Scenario: order for several pools all rejected
    Scenario: order for three pools one rejected, one not yet accepted, one accepted
    Scenario: order for one pool some items have been handed out
    Scenario: order for one pool all items have been handed out
    Scenario: order for one pool some items have been brought back
    Scenario: order for one pool all items have been brought back
    Scenario: order for a delegation user where I have sent the order
    Scenario: order for a delegation user where someone of my delegation has sent the order
    Scenario: delete my order (only for one pool not possible?)
    Scenario: the order has timed out - status?
    Scenario: An order for a deactivated pool exists

