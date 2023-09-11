Feature: Shopping Cart - Filling out title and purpose
  As a customer, when submitting the cart I have to enter title and purpose of Order 1.
  The purpose is prefilled with the text entered in the title (both fields show the same text in real-time),
  but only as long as something is entered into the purpose field.

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"

  Scenario: Title and purpose
    Given I have one item in the cart ready to be submitted
    And I log in as the user
    And I navigate to the cart

    When I click on "Send order"

    When I enter "Order 1" as title
    Then the purpose contains "Order 1"
    When I enter "for my diploma film" as purpose
    Then the title contains "Order 1"
    When I enter "Movie Shoot" as title
    Then the purpose contains "for my diploma film"

    When I submit the form
    And the "Send order" dialog has closed
    And I accept the "Order submitted" dialog with the text:
      """
      Order was submitted but still needs to be approved!
      Movie Shoot
      for my diploma film
      Between ${Date.today} and ${Date.tomorrow}, 1 item
      """
    Then I have been redirected to the orders list
    And the newly created order in the DB has:
      | title       | purpose             |
      | Movie Shoot | for my diploma film |

    When I click on the card with title "Movie Shoot"
    Then I see the page title "Movie Shoot"
    And I see "for my diploma film"

  Scenario: Lending terms
    Given lending term acceptance is turned on in settings
    And I have one item in the cart ready to be submitted

    And I log in as the user
    And I navigate to the cart
    And I sleep "0.5"
    And I click on "Send order"
    And I enter "Movie Shoot" in the "Title" field

    When I click on "Send"
    Then the "Send order" dialog did not close

    When I check "I accept the lending terms"
    And I click on "Send"
    Then I see the "Order submitted" dialog

  Scenario: Contact details
    Given contact details is turned on in settings
    And I have one item in the cart ready to be submitted

    And I log in as the user
    And I navigate to the cart
    And I sleep "0.5"
    And I click on "Send order"
    And I enter "Movie Shoot" in the "Title" field

    When I enter "Me at home" in the "Contact details" field
    And I click on "Send"
    And I accept the "Order submitted" dialog
    Then I have been redirected to the orders list

    When I click on the card with title "Movie Shoot"
    Then I see the following text in the "Contact details" section:
      """
      Me at home
      """
