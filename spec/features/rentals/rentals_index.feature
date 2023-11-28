Feature: Rentals

  Background:
    Given there is an initial admin
    And there is a user
    And there is a delegation "Delegation D"
    And the user is member of delegation "Delegation D"
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And there is an inventory pool "Pool B"
    And the user is customer of pool "Pool B"
    And there is a model "DSLR Camera"
    And there is a model "Tripod"
    And the following items exist:
      | code | model       | pool   |
      | A1   | DSLR Camera | Pool A |
      | B1   | DSLR Camera | Pool B |
      | B2   | Tripod      | Pool B |

  Scenario: Switching profile
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | user | quantity | model       | pool   | relative-start-date | relative-end-date | state    |
      | user | 1        | DSLR Camera | Pool A | ${Date.today}       | ${Date.tomorrow}  | approved |
    And a customer order with title "Order 2" and the following reservations exists for the user:
      | user         | quantity | model       | pool   | relative-start-date | relative-end-date   | state    |
      | Delegation D | 1        | DSLR Camera | Pool A | ${30.days.from_now} | ${31.days.from_now} | approved |
    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see the following orders:
      | title   |
      | Order 1 |
    And I click on the user profile button
    And I select "Delegation D" from "Switch Profile"
    Then the user profile button shows "DD"
    And I see the following orders:
      | title   |
      | Order 2 |

  Scenario: Filtering according state
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | quantity | model       | pool   | relative-start-date | relative-end-date | state    |
      | 1        | DSLR Camera | Pool A | ${Date.today}       | ${Date.tomorrow}  | approved |
    And a customer order with title "Order 2" and the following reservations exists for the user:
      | quantity | model       | pool   | relative-start-date | relative-end-date   | state     |
      | 1        | DSLR Camera | Pool A | ${30.days.from_now} | ${31.days.from_now} | approved  |
      | 1        | Tripod      | Pool B | ${30.days.from_now} | ${31.days.from_now} | submitted |
    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see the following orders:
      | title   |
      | Order 1 |
      | Order 2 |
    When I click on "Show search/filter"
    And I select "In approval" from "Status"
    And I click on "Apply"
    Then I see the following orders:
      | title   |
      | Order 2 |
    When I click on "Show search/filter"
    And I select "Pickup" from "Status"
    And I click on "Apply"
    Then I see the following orders:
      | title   |
      | Order 1 |
      | Order 2 |
    When I click on "Show search/filter"
    Then the "Status" select field contains value "Pickup"

  Scenario: Filtering according date (until date only)

    If only until date is given, then -infinity (or a date representing that) is implied for from date.
    For orders with state TO_RETURN or RETURNED, the pickup date (or the created date of contract) is
    taken as from date.

    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | quantity | model       | pool   | start-date | end-date   | state  | pickup-date |
      | 1        | DSLR Camera | Pool A | 1900-01-01 | 1900-01-02 | closed | 1900-01-01  |
    And a customer order with title "Order 2" and the following reservations exists for the user:
      | quantity | model       | pool   | start-date | relative-end-date | state  | pickup-date |
      | 1        | DSLR Camera | Pool A | 1900-01-01 | ${Date.tomorrow}  | signed | 1900-01-01  |
    And a customer order with title "Order 3" and the following reservations exists for the user:
      | quantity | model       | pool   | start-date | relative-end-date  | state  | pickup-date |
      | 1        | DSLR Camera | Pool A | 1900-01-01 | ${7.days.from_now} | signed | today       |
    And a customer order with title "Order 4" and the following reservations exists for the user:
      | quantity | model       | pool   | start-date | end-date   | state    | pickup-date |
      | 1        | DSLR Camera | Pool A | 9999-12-30 | 9999-12-31 | approved |             |
    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see the following orders:
      | title   |
      | Order 1 |
      | Order 2 |
      | Order 3 |
      | Order 4 |
    When I click on "Show search/filter"
    And I enter "day after tomorrow" in the "Until" field
    And I click on "Apply"
    Then I see the following orders:
      | title   |
      | Order 1 |
      | Order 2 |
      | Order 3 |
  # TODO: fix problem with capybara and input element!!!
  # When I click on "Show search/filter"
  # And I enter "yesterday" in the "Until" field
  # And I click on "Apply"
  # Then I see the following orders:
  #   | title   |
  #   | Order 1 |
  #   | Order 2 |
  # When I click on "Show search/filter"
  # Then the "Until" input field has value "yesterday"

  Scenario: Filtering according to inventory pool
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | quantity | model       | pool   | relative-start-date | relative-end-date | state    |
      | 1        | DSLR Camera | Pool A | ${Date.today}       | ${Date.tomorrow}  | approved |
    And a customer order with title "Order 2" and the following reservations exists for the user:
      | quantity | model       | pool   | relative-start-date | relative-end-date  | state     |
      | 1        | DSLR Camera | Pool A | ${30.day.from_now}  | ${31.day.from_now} | approved  |
      | 1        | Tripod      | Pool B | ${30.day.from_now}  | ${31.day.from_now} | submitted |
    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see the following orders:
      | title   |
      | Order 1 |
      | Order 2 |
    When I click on "Show search/filter"
    And I select "Pool A" from "Inventory pools"
    And I click on "Apply"
    Then I see the following orders:
      | title   |
      | Order 1 |
      | Order 2 |
    When I click on "Show search/filter"
    And I select "Pool B" from "Inventory pools"
    And I click on "Apply"
    Then I see the following orders:
      | title   |
      | Order 2 |
    When I click on "Show search/filter"
    Then the "Inventory pools" select field contains value "Pool B"

  Scenario: Filtering according to search term
    Given a customer order with title "Order 1" and the following reservations exists for the user:
      | quantity | model       | pool   | relative-start-date | relative-end-date | state    |
      | 1        | DSLR Camera | Pool A | ${Date.today}       | ${Date.tomorrow}  | approved |
    And a customer order with title "Order 2" and the following reservations exists for the user:
      | quantity | model       | pool   | relative-start-date | relative-end-date  | state     |
      | 1        | DSLR Camera | Pool A | ${30.day.from_now}  | ${31.day.from_now} | approved  |
      | 1        | Tripod      | Pool B | ${30.day.from_now}  | ${31.day.from_now} | submitted |
    When I log in as the user
    And I visit "/borrow/rentals/"
    Then I see the following orders:
      | title   |
      | Order 1 |
      | Order 2 |
    When I click on "Show search/filter"
    And I enter "Order 1" in the "Search term" field
    And I click on "Apply"
    Then I see the following orders:
      | title   |
      | Order 1 |
    When I click on "Show search/filter"
    Then the "Search term" input field has value "Order 1"
