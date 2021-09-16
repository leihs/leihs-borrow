Feature: Shopping Cart - Filling out title and purpose
  As a customer, when submitting the cart I have to enter title and purpose of my order.
  The purpose is prefilled with the text entered in the title (both fields show the same text in real-time),
  but only as long as something is entered into the purpose field.

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"

  @pending
  Scenario: Title and purpose
    Given I have one item in the cart ready to be submitted
    And I log in as the user
    And I navigate to the cart

    When I click on "Confirm rental"

    When I enter "My Order" as title
    Then the purpose contains "My Order"
    When I enter "for my diploma film" as purpose
    Then the title contains "My Order"
    When I enter "Movie Shoot" as title
    Then the purpose contains "for my diploma film"

    When I submit the form
    And I see the page title "Movie Shoot"
    Then I have been redirected to the newly created order
    And I see "for my diploma film"
    And the newly created order in the DB has:
      | title       | purpose             |
      | Movie Shoot | for my diploma film |

