Feature: Search results (and caching)

  Background:
    Given there is an initial admin
    And there is a delegation "Delegation D"
    And there is a user
    And the user is member of delegation "Delegation D"
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And the delegation "Delegation D" is customer of pool "Pool A"
    And there is an inventory pool "Pool B"
    And the user is customer of pool "Pool B"
    Given there are 40 different "Camera" models with a borrowable items in "Pool A"
    And all the "Camera" models belong to the "Cameras" category
    And there are 40 different "Beamer" models with a borrowable items in "Pool B"
    And all the "Beamer" models belong to the "Beamers" category
    Given I log in as the user
    And I wait for 1 second

  Scenario: Home page
    # Show all without filtering
    When I click on "Show search/filter"
    And I click button "Apply"
    Then I see 20 different "Beamer" models

    When I click 3 times on "Load more"
    Then I see 40 different "Beamer" models
    And I see 40 different "Camera" models
    And there is no "Load more" button

    # Clear filters, results and cache (!)
    # And I pry
    When I click on "Clear"
    # And I go to the homepage
    Then I see category "Cameras"
    And I see category "Beamers"

    When I click on "Show search/filter"
    And I click button "Apply"
    Then I see 20 different "Beamer" models
    And there is "Load more" button
    And I click on "Clear"

  Scenario: Filter with search term (and caching)
    # Filter with search term
    When I click on "Show search/filter"
    And I enter "Camera" in the search field
    And I click button "Apply"
    And I click on "Load more"
    Then I see 40 different "Camera" models
    And there is no "Load more" button

    # Filter with another search term (without clearing)
    When I enter "Beamer" in the search field
    And I click button "Apply"
    And I click on "Load more"
    Then I see 40 different "Beamer" models
    And there is no "Load more" button

    # Filter with previous search term:
    # All results are already cached and are refreshed accordingly.
    When I enter "Camera" in the search field
    And I click button "Apply"
    Then I see 40 different "Camera" models
    And there is no "Load more" button

  Scenario: Category page (implicit filter taken from the URL)
    # Filter with search term
    When I click on category "Cameras"
    And I click on "Load more"
    Then I see 40 different "Camera" models
    And there is no "Load more" button

    When I click on "Show search/filter"
    And I select pool "Pool B"
    And I click button "Apply"
    Then there are no results
    And there is no "Load more" button

    When I select all pools
    And I click button "Apply"
    Then I see 40 different "Camera" models
    And there is no "Load more" button

  Scenario: Filter available with quantity > 1
    # Filter with search term
    Given there is a model "Model A"
    And there are 2 borrowable items for model "Model A" in pool "Pool A"
    And there is a model "Model B"
    And there is 1 borrowable item for model "Model B" in pool "Pool A"

    When I click on "Show search/filter"
    And I choose to filter by availabilty
    And I choose next working day as start date
    And I choose next next working day as end date
    And I set the quantity to 2
    And I click button "Apply"
    Then I see 1 different "Model A" models
    And I don't see any "Model B" model
