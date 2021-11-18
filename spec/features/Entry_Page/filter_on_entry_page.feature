
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
        And the following order exists
            | title        | Purpose      | model       | pool   | quantity | start-date | end-date   |
            | blue my mind | blue my mind | DSLR Camera | Pool A | 2        | 2032-02-01 | 2032-02-03 |
            | blue my mind | blue my mind | Tripod      | Pool A | 1        | 2032-02-01 | 2032-02-04 |


    Scenario: Filter on Entry page
        When I log in as user
        And I am assigned to a delegation
        And I navigate to my rentals
        Then I see the filter is collapsed
        When I press on the filter
        Then the filter opens
        And I see a dropdown which contains my user and the delegation "Projekt Saalschutz"
        And I see a field to enter the search term
        And I see a drop down with pools listed where I have at least customer rights
        And I see a checkbox "Nur Verfügbare anzeigen"
        When I klick "Anwenden"
        Then all items are shown which match the filter selections

    Scenario: Use alle fields of filter on Entry page
        When I log in as user
        And I am assigned to a delegation
        And I navigate to my rentals
        Then I see the filter is collapsed
        When I press on the filter
        Then the filter opens
        When I select the delegation "Project Saalschutz"
        And I enter the search term "Camera"
        And I select the pool "Pool A"
        And I select the option "Nur Verfügbare anzeigen"
        When I klick "Anwenden"
        Then all items are shown which match the filter selections
        And I see that the filter does not have the default selections
        When I open the filter
        And I select "Zurücksetzen"
        Then I see all categories which contain Items I can borrow