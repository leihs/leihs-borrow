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
    When I click on "Filter"
    And I click button "Apply"
    Then I see 20 different "Beamer" models

    When I click 3 times on "Load more"
    Then I see 40 different "Beamer" models
    And I see 40 different "Camera" models
    And there is no "Load more" button

    # And I go to the homepage
    When I click on "Leihs"
    Then I see category "Cameras"
    And I see category "Beamers"

    When I click on "Filter"
    And I click button "Apply"
    Then I see 40 different "Beamer" models
    And I see 40 different "Camera" models
    And there is no "Load more" button

  Scenario: Search term entered into main input field can be submitted with enter key
    When I enter "Camera" in the main search field
    And I press the enter key
    Then the title of the page is "Search results"
    And I see 20 different "Camera" models
    And the search field contains "Camera"

  Scenario: Filter with search term (and caching)
    # Filter with search term
    When I click on "Filter"
    And I enter "Camera" in the search field
    And I click button "Apply"
    And I click on "Load more"
    Then I see 40 different "Camera" models
    And there is no "Load more" button

    # Filter with another search term (without clearing)
    When I click on "Filter"
    And I enter "Beamer" in the search field
    And I click button "Apply"
    And I click on "Load more"
    Then I see 40 different "Beamer" models
    And there is no "Load more" button

    # Filter with previous search term:
    # All results are already cached and are refreshed accordingly.
    When I click on "Filter"
    And I enter "Camera" in the search field
    And I click button "Apply"
    Then I see 40 different "Camera" models
    And there is no "Load more" button

  Scenario: Category page (implicit filter taken from the URL)
    # Filter with search term
    When I click on category "Cameras"
    And I click on "Load more"
    Then I see 40 different "Camera" models
    And there is no "Load more" button

    When I click on "Filter"
    And I select pool "Pool B"
    And I click button "Apply"
    Then there are no results
    And there is no "Load more" button

    When I click on "Filter"
    And I select all pools
    And I click button "Apply"
    Then I see 40 different "Camera" models
    And there is no "Load more" button

  Scenario: Category pages, navigation keeps the filters
    Given there is a subcategory "Analog Cameras" in "Cameras"
    And there is a subcategory "Medium Format" in "Analog Cameras"
    And there is a model "Hasselblad 500C"
    And the model "Hasselblad 500C" belongs to the "Medium Format" category
    And the model "Hasselblad 500C" has 1 borrowable item in "Pool A"

    When I click on category "Cameras"
    And I click on "Filter"
    And I enter "Hasselblad" in the search field
    And I click button "Apply"
    Then I see 1 "Hasselblad 500C" model

    When I expand the "Sub-categories" section
    And I click on the card with title "Analog Cameras"
    Then the title of the page is "Analog Cameras"
    And I see the following breadcrumbs:
      | category |
      | Cameras  |
    And I see 1 "Hasselblad 500C" model
    And the search field contains "Hasselblad"

    # FIXME: why is "Medium Format" subcategory no shown? ideally test should go 2 levels deep

    When I click on "Cameras" within breadcrumbs
    Then the title of the page is "Cameras"
    And I don't see any breadcrumbs
    And I see 1 "Hasselblad 500C" model
    And the search field contains "Hasselblad"

  Scenario: Filter available with quantity > 1
    # Filter with search term
    Given there is a model "Model A"
    And there are 2 borrowable items for model "Model A" in pool "Pool A"
    And there is a model "Model B"
    And there is 1 borrowable item for model "Model B" in pool "Pool A"

    When I click on "Filter"
    And I choose to filter by availabilty
    And I enter the date "${Date.today}" in the "From" field
    And I enter the date "${Date.tomorrow}" in the "Until" field
    And I set the quantity to 2
    And I click button "Apply"
    Then I see 1 different "Model A" models
    And I don't see any "Model B" model
