# TODO: uses this scenario to help develop and verify the UI.
# Optionally also extend this to be a real spec:
# * implement expectations for display
# * interact with each invalid line and resolve or remove it, then submit the cart

Feature: Shopping Cart - Error cases for Reservations

  As a customer, the reservations in my cart can be either valid or invalid.

  Invalid reservations do not reserve an item (they are not considered for availability calculation),
  nor is their timeout refreshed – they are considered "Drafts" thatI have to resolve or remove.

  As long as there is at least 1 invalid reservation in my cart, I cannot submit it.
  Each invalid line is marked and shows the appropriate error message, the bottom of the form shows the number of problems.
  There are 2 types of invalid reservations: resolvable und unresolvable.

  Resolvable invalid reservation can be changed in the cart (for example  by picking different dates or quantity),
  and thus become valid reservations again.
  Unresolvable invalid reservations can only be removed from the cart, for example when no items exist anymore
  or there is no pool I could reserve them from (for example when I was suspended or my access was removed completely).


  Background:
    # NOTE: because of the complex data setup, we differ from our ususal strategy and just load some opaque dataset.
    # For details about the specific test cases look up the source of "shared/refresh_timeout_data.rb".
    Given the refresh timeout spec data is loaded

  Scenario: Display of Error cases
    Given I log in as the user
    And I navigate to the cart

    And I pry
