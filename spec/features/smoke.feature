Feature: Borrow Smoke

  Background:
    Given there is a user with an ultimate access

  Scenario: Access borrow app
    Given I log in as the user
    When I visit "/app/borrow/"
    Then the borrow app is loaded
