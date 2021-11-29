Feature: Calendar Display of availability data

  …

  Scenario: Calendar Display of availability data

    Given these inventory pools exist:
      | Movie Pool   |
      | Another Pool |

    And these global settings exist:
      | max_reservation_time | 33 |

    And these settings are configured for inventory pool "Movie Pool":
      | reservation_advance_days | 1   |
      | max_visits               | 100 |

    And these workdays are configured for inventory pool "Movie Pool":
      | day       | open   | max_visits |
      | Monday    | OPEN   | 100        |
      | Tuesday   | OPEN   | 100        |
      | Wednesday | OPEN   | 100        |
      | Thursday  | OPEN   | 100        |
      | Friday    | OPEN   | 100        |
      | Saturday  | CLOSED | 100        |
      | Sunday    | CLOSED | 100        |