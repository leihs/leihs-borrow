Feature: Favorites

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And there is a model "Camera"
    And there is 1 borrowable item for model "Camera" in pool "Pool A"
    And there is a model "Beamer"
    And there is 1 borrowable item for model "Beamer" in pool "Pool A"
    Given I log in as the user

  Scenario: Whole cycle of favor, check, unfavor, check
    When I visit "/borrow/models/favorites"
    Then I see "No favorites added yet"

    When I visit "/borrow/"
    And I click on "Filter"
    And I click on "Apply"
    And I click on model "Beamer"
    And I click on "Add to favorites"
    When I visit "/borrow/models/favorites"
    Then I see 1 favorite
    And I see model "Beamer"

    When I visit "/borrow/"
    And I click on "Filter"
    And I click on "Apply"
    And I click on model "Camera"
    And I click on "Add to favorites"
    When I visit "/borrow/models/favorites"
    Then I see 2 favorites
    And I see model "Beamer"
    And I see model "Camera"

    When I visit "/borrow/"
    And I click on "Filter"
    And I click on "Apply"
    And I click on model "Beamer"
    And I click on "Remove from favorites"
    When I visit "/borrow/models/favorites"
    Then I see 1 favorite
    And I see model "Camera"

    When I visit "/borrow/"
    And I click on "Filter"
    And I click on "Apply"
    And I click on model "Camera"
    And I click on "Remove from favorites"
    When I visit "/borrow/models/favorites"
    Then I see "No favorites added yet"
