Feature: Request framework

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And there is a borrowable item in pool "Pool A"

  Scenario: Login and retry after having been logged out
    Given I log in as the user
    # FIXME! ###############################################
    # The sleeps are needed, otherwise errors are shown and
    # elements are missing.
    And I sleep "0.25"
    When I visit the model show page for the borrowable item
    And I sleep "0.25"
    And I clear the browser cookies
    And I sleep "0.25"
    And I click on "Add item"
    And the order panel is shown
    And I click on Add and I approve the alert
    ########################################################
    Then I see one retry banner
    And I see one login banner

    # 1st Retry (still logged out)
    When I click on retry and I approve the alert
    Then I see one retry banner
    And I see one login banner

    # Login
    When I login in via the login banner
    Then I don't see any login banner
    But I see one retry banner

    # 2nd Retry (now logged in)
    When I click on retry
    And I accept the "Item added" dialog
    And the "Item added" dialog has closed
    Then I don't see any login banner
    And I don't see any retry banner
    And the cart is not empty
