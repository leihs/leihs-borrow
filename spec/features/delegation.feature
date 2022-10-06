Feature: Delegations

  Background:
    Given there is an initial admin
    And there is a delegation "Delegation D"
    And there is a user "User A"
    And the user is member of delegation "Delegation D"
    And there is an inventory pool "Pool A"
    And the user is inventory manager of pool "Pool A"
    And there is an inventory pool "Pool B"
    And the delegation "Delegation D" is customer of pool "Pool B"

  Scenario: Switching between personal and delegation profile
    Given I log in as the user

    When I click on the user profile button
    And I click on "DD"
    Then the user profile button shows "DD"

    When I click on the user profile button
    And I click on "UA"
    Then the user profile button shows "UA"


  Scenario: Delegations in user account
    Given I log in as the user

    When I visit "/app/borrow/current-user"
    And I see the page title "User Account"

    Then I see the following lines in the "Delegations" section:
      | title        |
      | Delegation D |

  Scenario: Model available depending on profile
    Given there is a model "Kamera"
    And there is 1 borrowable item for model "Kamera" in pool "Pool A"
    And there is a model "Beamer"
    And there is 1 borrowable item for model "Beamer" in pool "Pool B"
    And I log in as the user
    And the user profile button shows "UA"

    When I click on "Filter"
    And I click button "Apply"
    Then I see one model with the title "Kamera"

    When I click on "Kamera"
    And I see the page title "Kamera"
    And I click on "Add item"
    Then the order panel is shown

    When I click on "Cancel"
    And I click on the user profile button
    And I click on "DD"
    And the user profile button shows "DD"
    Then the "Add item" button is disabled
    And I see the text:
      """
      Item not available for current profile
      """

    When I visit "/app/borrow/"
    And I click on "Filter"
    And I click button "Apply"
    Then I see one model with the title "Beamer"

    When I click on "Beamer"
    And I see the page title "Beamer"
    And I click on "Add item"
    Then the order panel is shown

    When I click on "Cancel"
    And I click on the user profile button
    And I click on "UA"
    And the user profile button shows "UA"
    Then the "Add item" button is disabled
    And I see the text:
      """
      Item not available for current profile
      """

  Scenario: Open a new window as delegation user
    Given I log in as the user

    When I click on the user profile button
    And I click on "DD"
    Then the user profile button shows "DD"

    When I switch to a new window
    And I visit "/app/borrow/"
    Then the user profile button shows "DD"

    # Check that changing the profile in window 2 does not change window 1
    When I click on the user profile button
    And I click on "UA"
    And the user profile button shows "UA"
    And I go back to the first window
    And I visit "/app/borrow/order"
    Then the user profile button shows "DD"

  Scenario: Open a new window as personal user
    Given I log in as the user
    Then the user profile button shows "UA"

    When I switch to a new window
    And I visit "/app/borrow/"
    Then the user profile button shows "UA"

    # Check that changing the profile in window 2 does not change window 1
    When I click on the user profile button
    And I click on "DD"
    And the user profile button shows "DD"
    And I go back to the first window
    And I visit "/app/borrow/order"
    Then the user profile button shows "UA"