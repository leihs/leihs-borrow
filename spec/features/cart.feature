Feature: Cart

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"

  Scenario: Title and purpose
    Given I have one item in the cart ready to be submitted
    And I log in as the user
    And I navigate to the cart 
    When I enter "foo" as title
    Then the purpose contains "foo"
    When I enter "foo bar" as purpose
    Then the title contains "foo"
    When I enter "baz" as title
    Then the purpose contains "foo bar"
    When I click on "Confirm order"
    Then I have been redirected to the newly created order
    And the newly created order has title "baz"
    And the newly created order has purpose "foo bar"
