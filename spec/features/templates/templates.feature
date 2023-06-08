Feature: Templates

  Background:
    Given there is an initial admin

    And there is an inventory pool "Pool A"
    And there is an inventory pool "Pool B"

    And there is a category "Video"
    And there is a category "Animals"

    And there is a model "Video Camera"
    And the "Video Camera" model belongs to category "Video"
    And there is 1 borrowable item for model "Video Camera" in pool "Pool A"
    And there is 1 borrowable item for model "Video Camera" in pool "Pool B"

    And there is a model "Gecko"
    And the "Gecko" model belongs to category "Animals"
    And there is 1 borrowable item for model "Gecko" in pool "Pool A"
    And there is 1 borrowable item for model "Gecko" in pool "Pool B"

    And there is a model "Unicorn"
    And the "Unicorn" model belongs to category "Animals"
    # Note: no borrowable items

    And the inventory pool "Pool A" has a template called "Recommendation of the day" with the following models:
      | product      |
      | Video Camera |
      | Gecko        |

    And the inventory pool "Pool A" has a template called "Our classics" with the following models:
      | product      |
      | Video Camera |
      | Unicorn      |

    And the inventory pool "Pool A" has a template called "Extinct" with the following models:
      | product |
      | Unicorn |

    And there is a user "User A"
    And the user is customer of pool "Pool A"

    And there is a delegation "Delegation D"
    And the delegation is customer of pool "Pool B"
    And the user is member of delegation "Delegation D"

  Scenario: Browsing templates
    When I log in as the user
    Then I see the following categories:
      | caption   |
      | Animals   |
      | Video     |
      | Templates |

    # Template list
    When I click on the item captioned "Templates" in the category list
    Then I see the following lines in the page content:
      | title                     | body   |
      | Extinct                   | Pool A |
      | Our classics              | Pool A |
      | Recommendation of the day | Pool A |

    # Template detail
    When I click on the card with title "Recommendation of the day"
    Then I see the page title "Template"
    And the page subtitle is "Recommendation of the day"
    And I see "Items"
    And I see the following models:
      | caption         |
      | 1× Gecko        |
      | 1× Video Camera |

    # Switch to delegation (which has access only to Pool B which does not offer templates)
    When I click on "Catalog"
    And I click on the user profile button
    And I click on "DD"

    Then I see the following categories:
      | caption |
      | Animals |
      | Video   |

    When I visit "/borrow/templates/"
    Then I see "No templates available for the current profile"

  Scenario: Ordering by template
    When I log in as the user
    And I click on the item captioned "Templates" in the category list
    And I click on the card with title "Recommendation of the day"
    And I click on "Order items"

    Then I see the "Add items" dialog
    And I see "2 items will be added to the cart."
    And I see the following text in the dialog:
      """
      Order for
      User A (personal)
      """
    And I see the date "${Date.today}" in the "From" field
    And I see the date "${Date.tomorrow}" in the "Until" field

    When I enter the date "${3.day.from_now}" in the "From" field
    And I enter the date "${4.day.from_now}" in the "Until" field
    And I accept the "Add items" dialog
    Then the "Add items" dialog has closed
    And I see the "Items added" dialog with the text:
      """
      2 items were added to the cart and can be reviewed/edited there.
      """

    When I click on "Go to cart"
    Then the "Items added" dialog has closed
    And I see the page title "Cart"
    And I see the following lines in the "Items" section:
      | title           | body   | foot                          |
      | 1× Gecko        | Pool A | 2 days from ${3.day.from_now} |
      | 1× Video Camera | Pool A | 2 days from ${3.day.from_now} |


  Scenario: Template with an unreservable model
    When I log in as the user
    And I click on the item captioned "Templates" in the category list
    And I click on the card with title "Our classics"
    Then I see the page title "Template"
    And the page subtitle is "Our classics"
    And I see "Items"
    And I see "The items shown in grey font are not reservable for the current profile"
    And I see the following models:
      | caption         |
      | 0× Unicorn      |
      | 1× Video Camera |

    When I click on "Order items"
    Then I see the "Add items" dialog
    And I see "One item will be added to the cart."

    When I accept the "Add items" dialog
    Then the "Add items" dialog has closed
    And I see the "Items added" dialog with the text:
      """
      One item was added to the cart and can be reviewed/edited there.
      """

    When I click on "Go to cart"
    Then the "Items added" dialog has closed
    And I see the page title "Cart"
    And I see the following lines in the "Items" section:
      | title           | body   | foot                           |
      | 1× Video Camera | Pool A | 2 days from ${0.days.from_now} |

  Scenario: Template no reservable models
    When I log in as the user
    And I click on the item captioned "Templates" in the category list
    And I click on the card with title "Extinct"
    Then I see the page title "Template"
    And the page subtitle is "Extinct"
    And I see "Items"
    And I see "This template does not contain any items that can be reserved for the current profile"
    And I see the following models:
      | caption    |
      | 0× Unicorn |
    And I don't see "Order items"

  Scenario: Template forbidden for profile
    When I log in as the user
    And I click on the item captioned "Templates" in the category list
    And I click on the card with title "Recommendation of the day"
    And I see the page title "Template"
    And the page subtitle is "Recommendation of the day"
    And I click on the user profile button
    And I click on "DD"

    Then I see the page title "Template"
    And I see "This template is not available for the current profile"
    And I don't see "Items"
