Feature: Borrow Entitlement

  TODO - the following is just a draft in prose

  Background:
    Given there is a category "Film"
    And there is an inventory pool "Filmpool"

    And there is a model "Normal Camera"
    And the "Normal Camera" model belongs to category "Film"
    And there are 2 borrowable items for model "Normal Camera" in pool "Filmpool"

    And there is a model "Better Camera"
    And the "Better Camera" model belongs to category "Film"
    And there are 2 borrowable items for model "Better Camera" in pool "Filmpool"

    And there is a model "Super Camera"
    And the "Super Camera" model belongs to category "Film"
    And there are 2 borrowable items for model "Super Camera" in pool "Filmpool"

    And there is an entitlement group "Famous directors" in pool "Filmpool"
    And the group "Famous directors" is entitled for 1 item of model "Better Camera"
    And the group "Famous directors" is entitled for 2 items of model "Super Camera"

  Scenario: User without entitlement
    Given there is a user "Normin Normalo"
    And the user is customer of pool "Filmpool"
    # ...and is not entitled

    When I log in as the user
    And I click on "Film"
    Then I see the following models:
      | caption       |
      | Better Camera |
      | Normal Camera |

    When I enter "camera" in the main search field
    And I press the enter key
    Then I see the following models:
      | caption       |
      | Better Camera |
      | Normal Camera |

    # Normal Camera
    When I click on "Normal Camera"
    And I click on "Add item"
    Then I see the text:
      """
      Maximum available amount: 2
      """
    And I click on "Cancel"

    # Better Camera
    When I click on "Catalog"
    And I click on "Film"
    And I click on "Better Camera"
    And I click on "Add item"
    Then I see the text:
      """
      Maximum available amount: 1
      """

    # Super Camera (not findable via normal navigation for this user)
    When I visit the model show page of model "Super Camera"
    Then I see the text:
      """
      Item not available for current profile
      """

  Scenario: User with entitlement
    Given there is a user "Sofia Coppola"
    And the user is customer of pool "Filmpool"
    And the user is member of entitlement group "Famous directors"

    When I log in as the user
    And I click on "Film"
    Then I see the following models:
      | caption       |
      | Better Camera |
      | Normal Camera |
      | Super Camera  |

    When I enter "camera" in the main search field
    And I press the enter key
    Then I see the following models:
      | caption       |
      | Better Camera |
      | Normal Camera |
      | Super Camera  |

    # Normal Camera
    When I click on "Normal Camera"
    And I click on "Add item"
    Then I see the text:
      """
      Maximum available amount: 2
      """
    And I click on "Cancel"

    # Better Camera
    When I click on "Catalog"
    And I click on "Film"
    And I click on "Better Camera"
    And I click on "Add item"
    Then I see the text:
      """
      Maximum available amount: 2
      """
    And I click on "Cancel"

    # Super Camera (not findable via normal navigation for this user)
    When I click on "Catalog"
    And I click on "Film"
    And I click on "Super Camera"
    And I click on "Add item"
    Then I see the text:
      """
      Maximum available amount: 2
      """
