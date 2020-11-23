Feature: Search Results

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

  Scenario: Main
    Given there are 40 different "Kamera" models with a borrowable items in "Pool A"
    And there are 40 different "Beamer" models with a borrowable items in "Pool B"
    When I log in as the user
    And I click button "Get Results"
    Then I see 20 different "Beamer" models
    When I click 3 times on "Load more"
    Then I see 40 different "Beamer" models
    And I see 40 different "Kamera" models
    And there is no "Load more" button
