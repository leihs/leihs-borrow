Feature: Search results (and caching)

  Background:
    Given there is an initial admin
    And there is a user
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"

    And there is a model "Video Camera"
    And there is 1 borrowable item for model "Video Camera" in pool "Pool A"
    And there is a category "Video"
    And the "Video Camera" model belongs to category "Video"

    And there is a model "Beamer"
    And there is 1 borrowable item for model "Beamer" in pool "Pool A"
    And there is a category "Beamers"
    And the "Beamer" model belongs to category "Beamers"

    And there is a model "Mini Beamer"
    And there is 1 borrowable item for model "Mini Beamer" in pool "Pool A"
    And there is a category "Mini Beamers"
    And the "Mini Beamer" model belongs to category "Mini Beamers"

    And parent of category "Mini Beamers" is category "Beamers"
    And parent of category "Beamers" is category "Video"

    Given I log in as the user

  Scenario: Navigation and breadcrumbs
    When I click on category "Video"
    Then the title of the page is "Video"
    And I see 3 models
    And I see model "Video Camera"
    And I see model "Beamer"
    And I see model "Mini Beamer"
    And don't see any breadcrumbs

    When I click on sub-category "Beamers"
    Then the title of the page is "Beamers"
    And I see 2 models
    And I see model "Beamer"
    And I see model "Mini Beamer"
    And I see the following breadcrumb:
      | category |
      | Video    |

    When I click on sub-category "Mini Beamers"
    Then the title of the page is "Mini Beamers"
    And I see 1 model
    And I see model "Mini Beamer"
    And I see the following breadcrumb:
      | category |
      | Video    |
      | Beamers  |

    When I click on "Beamers" within breadcrumbs
    Then the title of the page is "Beamers"
    And I see 2 models
    And I see model "Beamer"
    And I see model "Mini Beamer"
    And I see the following breadcrumb:
      | category |
      | Video    |

    When I click on "Video" within breadcrumbs
    Then the title of the page is "Video"
    And I see 3 models
    And I see model "Video Camera"
    And I see model "Beamer"
    And I see model "Mini Beamer"
    And don't see any breadcrumbs

  Scenario: Breadcrumbs when coming directly to a sub-category
    When visit the sub-category "Mini Beamers"
    Then the title of the page is "Mini Beamers"
    And I see 1 model
    And I see model "Mini Beamer"
    And I see the following breadcrumb:
      | category |
      | Video    |
      | Beamers  |
    And I pry

    When I click on "Beamers" within breadcrumbs
    Then the title of the page is "Beamers"
    And I see 2 models
    And I see model "Beamer"
    And I see model "Mini Beamer"
    And I see the following breadcrumb:
      | category |
      | Video    |

    When I click on "Video" within breadcrumbs
    Then the title of the page is "Video"
    And I see 3 models
    And I see model "Video Camera"
    And I see model "Beamer"
    And I see model "Mini Beamer"
    And don't see any breadcrumbs
