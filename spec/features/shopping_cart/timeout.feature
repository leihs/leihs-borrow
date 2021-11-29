# TODO: some scenarios have a 1min wait step… they should run in parallel in CI.
#       so either split up this file or improve cider config (like in legacy)

# TODO: make sure this works / there are scenarios for "reactivate order" and "from template"

Feature: Shopping Cart - Timeout
  Reservations (shopping cart lines) are only honored for a certain time (configurable, default 30 minutes).
  After that time has passed, the line is still saved in the database, but the reservation is not active,
  so other users can reserve those items instead.
  While the cart has not timed out, each interaction with the app resets the timeout.
  On the first interaction after a timeout, reservations that still can be honored are automatically
  re-activated.
  Reservations that could not be automatically reactivated are kept as conflicts which need to be solved by
  the user before submitting the cart (this can happen if another user has reserved the same items and they are
  now unavailable, but theoretically could also happen when items are retired etc.)
  Reservations are only refreshed when the full quantity is available ("all or nothing"), the user has to decide
  for themselves if they want to decrease the quantity or choose another conflict resolution

  Background:
    Given there is an initial admin
    And there is a user "Normin Normalo"
    And there is an inventory pool "Pool A"
    And the user is customer of pool "Pool A"
    And the global reservation timeout is "1" minute
    And the following inventory exists:
      | model       | code  | pool   |
      | DSLR Camera | AV001 | Pool A |
      | DSLR Camera | AV002 | Pool A |
      | DSLR Camera | AV004 | Pool A |
      | Tripod      | AV005 | Pool A |
      | Tripod      | AV006 | Pool A |

  # Scenario: Cart timeout is reset on each interaction
  #   When I pry
  # # check, go to homepage, check, go to user profile, check


  Scenario: Cart Timeout is displayed as time or "expired", and can be prolonged by clicking the button
    Given the following reservations exist for the user:
      | quantity | model       | pool   | start-date | end-date   |
      | 1        | DSLR Camera | Pool A | 2032-02-01 | 2032-02-03 |

    When I log in as the user
    And I navigate to the cart
    Then I see these lines of text in the "Status" section:
      | Time limit                |
      | Less than one minute left |

    When I wait 60 seconds
    Then I see these lines of text in the "Status" section:
      | Time limit |
      | Expired    |

    When I click on "Reset time limit"
    Then I see these lines of text in the "Status" section:
      | Time limit                |
      | Less than one minute left |

  Scenario: Cart Timeout is is prolonged by any navigation
    Given the global reservation timeout is "2" minutes
    And the following reservations exist for the user:
      | quantity | model       | pool   | start-date | end-date   |
      | 1        | DSLR Camera | Pool A | 2032-02-01 | 2032-02-03 |

    When I log in as the user
    And I navigate to the cart
    Then I see these lines of text in the "Status" section:
      | Time limit    |
      | 1 minute left |

    When I wait 60 seconds
    Then I see these lines of text in the "Status" section:
      | Time limit                |
      | Less than one minute left |

    When I click on the "Leihs logo" in the main navigation
    And I click on the "cart icon" in the main navigation
    Then I see these lines of text in the "Status" section:
      | Time limit    |
      | 1 minute left |


  Scenario: Timed out cart lines are automatically reserved again where possible
    Situation:
    2 cameras were reserved by Normin, 40 minutes ago.
    2 cameras were reserved by Karl after Normins reservation timed out.
    Now only 1 camera is available anymore – Normins reservation shows a conflict that needs to be solved (meanwhile NO cameras are reserved, its "all or nothing".)
    The Tripod reservation has no conflict.

    Given the global reservation timeout is "30" minutes
    And there is a user "Normin Normalo"
    And there is a user "Karl Klammer"
    And the user "Karl Klammer" is customer of pool "Pool A"

    And the following reservations exist for the user "Normin Normalo":
      | quantity | model       | pool   | relative-updated-at | relative-start-date | relative-end-date  |
      | 2        | DSLR Camera | Pool A | ${40.minutes.ago}   | ${1.day.from_now}   | ${2.days.from_now} |
      | 1        | Tripod      | Pool A | ${40.minutes.ago}   | ${1.day.from_now}   | ${2.days.from_now} |

    And the following reservations exist for the user "Karl Klammer":
      | quantity | model       | pool   | relative-updated-at | relative-start-date | relative-end-date  |
      | 3        | DSLR Camera | Pool A | ${10.minutes.ago}   | ${1.day.from_now}   | ${2.days.from_now} |

    When I log in as the user "Normin Normalo"
    And I navigate to the cart
    And I pry
    Then I see the following lines in the "Items" section:
      | title          | body   | foot                                                |
      | 1× DSLR Camera | Pool A | 2 days from #{1.day.from_now.strftime('%-d/%m/%y')} |
      | 1× Tripod      | Pool A | 2 days from 06/11/21                                |
# Then I see the following lines in the "Items" section:
#   | title          | body   | foot                 |
#   | 1× DSLR Camera | Pool A | 2 days from 06/11/21 |
#   | 1× Tripod      | Pool A | 2 days from 06/11/21 |

# check cart icon



#   Scenario: time out with conflicts, resolve the conflicts
# # 1 line reduce quantity (reservation by other user)
# # 1 line remove (item was retired)






#   Scenario: Timed out cart lines are conflicting when reservation is in the past
#   Reservation is for past date - can never be (automatically) re-activated
#     I need to adjust the dates to be in the future, than i can save


# Scenario: When the cart only contains "Draft" reservations, the Time Limit is not active
#           (no or grayed out progress bar, disabled "reset time limit" button, maybe some message)