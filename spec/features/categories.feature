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
    And I resize the window to mobile size

  Scenario: Navigation and breadcrumbs
    When I click on category "Video"
    Then the title of the page is "Video"
    And I see 3 models
    And I see model "Video Camera"
    And I see model "Beamer"
    And I see model "Mini Beamer"
    And don't see any breadcrumbs

    When I expand the "Sub-categories" section
    And I click on sub-category "Beamers"
    Then the title of the page is "Beamers"
    And I see 2 models
    And I see model "Beamer"
    And I see model "Mini Beamer"
    And I see the following breadcrumbs:
      | category |
      | Video    |

    When I expand the "Sub-categories" section
    And I click on sub-category "Mini Beamers"
    Then the title of the page is "Mini Beamers"
    And I see 1 model
    And I see model "Mini Beamer"
    And I see the following breadcrumbs:
      | category |
      | Video    |
      | Beamers  |

    When I click on "Beamers" within breadcrumbs
    Then the title of the page is "Beamers"
    And I see 2 models
    And I see model "Beamer"
    And I see model "Mini Beamer"
    And I see the following breadcrumbs:
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
    When I visit the sub-category "Mini Beamers"
    Then the title of the page is "Mini Beamers"
    And I see 1 model
    And I see model "Mini Beamer"
    And I see the following breadcrumbs:
      | category |
      | Video    |
      | Beamers  |

    When I click on "Beamers" within breadcrumbs
    Then the title of the page is "Beamers"
    And I see 2 models
    And I see model "Beamer"
    And I see model "Mini Beamer"
    And I see the following breadcrumbs:
      | category |
      | Video    |

    When I click on "Video" within breadcrumbs
    Then the title of the page is "Video"
    And I see 3 models
    And I see model "Video Camera"
    And I see model "Beamer"
    And I see model "Mini Beamer"
    And don't see any breadcrumbs

  Scenario: Expand and collapse sub-categories

    The app tracks the collapse/expand state of the "Sub-categories" section.
    Initially it must be collapsed.

    When I click on category "Video"
    Then the title of the page is "Video"
    And the "Sub-categories" section is collapsed

    When I expand the "Sub-categories" section
    And I click on sub-category "Beamers"
    Then the title of the page is "Beamers"
    And the "Sub-categories" section is collapsed

    When I click on "Video" within breadcrumbs
    Then the title of the page is "Video"
    And the "Sub-categories" section is expanded

    When I collapse the "Sub-categories" section
    And I visit the sub-category "Beamers"
    And the title of the page is "Beamers"
    And I click on "Video" within breadcrumbs
    And the title of the page is "Video"
    Then the "Sub-categories" section is collapsed

  Scenario: Show subcategories with models deep within

    Let's assume that:

    1. Any shown category is at level "root".
    2. The category only has 1st level subcategories where each 1st level subcategory
    don't have a direct model assigned, but any number of models may be assigned to
    any of the subcategory's descendant categories.

    The shown category displays an aggregate of all models either assigned to itself
    or to any descendant category.

    The shown category displays all its sub-categories where 2. applies.

    Testdata:

    Audio -> "Mikrofon Meteor Mic"
    Audio -> Speakers -> Active Speakers -> "Active Speaker Anchor AN-100"
    Audio -> Speakers -> Passive Speakers -> Subwoofers -> "Subwoofer Genelec 7050"

    Given there is a category "Audio"
    And there is a model "Mikrofon Meteor Mic"
    And the "Mikrofon Meteor Mic" model belongs to category "Audio"
    And there is a category "Speakers"
    And parent of category "Speakers" is category "Audio"
    And there is a category "Active Speakers"
    And parent of category "Active Speakers" is category "Speakers"
    And there is a model "Active Speaker Anchor AN-100"
    And the "Active Speaker Anchor AN-100" model belongs to category "Active Speakers"
    And there is a category "Passive Speakers"
    And parent of category "Passive Speakers" is category "Speakers"
    And there is a category "Subwoofers"
    And parent of category "Subwoofers" is category "Passive Speakers"
    And there is a model "Subwoofer Genelec 7050"
    And the "Subwoofer Genelec 7050" model belongs to category "Subwoofers"

    And there is 1 borrowable item for model "Mikrofon Meteor Mic" in pool "Pool A"
    And there is 1 borrowable item for model "Subwoofer Genelec 7050" in pool "Pool A"
    And there is 1 borrowable item for model "Active Speaker Anchor AN-100" in pool "Pool A"

    # root
    When I visit "/borrow/"
    And I click on category "Audio"
    Then I see 3 models
    And I see model "Mikrofon Meteor Mic"
    And I see model "Subwoofer Genelec 7050"
    And I see model "Active Speaker Anchor AN-100"

    # 1st level
    When I expand the "Sub-categories" section
    And I see 1 sub-category
    And I click on sub-category "Speakers"
    Then I see 2 models
    And I see model "Subwoofer Genelec 7050"
    And I see model "Active Speaker Anchor AN-100"

    # 2nd level
    When I expand the "Sub-categories" section
    And I see 2 sub-categories
    And I click on sub-category "Active Speakers"
    Then I see 1 model
    And I see model "Active Speaker Anchor AN-100"
    And I don't see "Sub-categories"
    And I click on "Speakers"
    And I click on sub-category "Passive Speakers"
    Then I see 1 model
    And I see model "Subwoofer Genelec 7050"

    # 3rd level
    When I expand the "Sub-categories" section
    And I see 1 sub-category
    And I click on sub-category "Subwoofers"
    Then I see 1 model
    And I see model "Subwoofer Genelec 7050"
    And I don't see "Sub-categories"
