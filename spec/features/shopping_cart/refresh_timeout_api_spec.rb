require "spec_helper"
require_relative "../../graphql/graphql_helper"
require_relative "./invalid_reservations_data_setup"

# Switch to launch a browser to visualize the sample data in UI
DEVELOP_UI = false

if DEVELOP_UI
  require_relative "../shared/common" # NOTE: only needed for development, provides `log_in_as_user_with_email` helper
end

describe "refresh timeout", type: (DEVELOP_UI ? :feature : nil) do
  include_context "invalid reservations data setup"

  it "detects invalid reservations and renews the timeout period for valid ones" do
    pending "FIX User Suspension"

    # Create all sample reservations in the database

    expect(create_all_sample_reservations).to_not be_nil

    # Apply mutation query to sample reservations

    refresh_timeout_mutation =
      <<-GRAPHQL
        mutation {
          refreshTimeout {
            unsubmittedOrder {
              validUntil
              reservations {
                id
                status
              }
              invalidReservationIds
            }
          }
        }
      GRAPHQL
    m_result = query(refresh_timeout_mutation, user.id).deep_symbolize_keys
    expect(m_result[:errors]).to be_nil

    # Get data returned from query

    reservations = m_result.dig(:data, :refreshTimeout, :unsubmittedOrder, :reservations)
    valid_until = if vu = m_result.dig(:data, :refreshTimeout, :unsubmittedOrder, :validUntil)
        DateTime.parse(vu)
      end
    unsubmitted_ids = reservations.select { |r| r[:status] == "UNSUBMITTED" }.map { |r| r[:id] }
    draft_ids = reservations.select { |r| r[:status] == "DRAFT" }.map { |r| r[:id] }
    invalid_ids = m_result.dig(:data, :refreshTimeout, :unsubmittedOrder, :invalidReservationIds)

    # When configured, open UI in browser

    if DEVELOP_UI
      log_in_as_user_with_email(user.email)
      visit "/app/borrow/order"

      # dev_take_screenshots_of_each_order_panel
      binding.pry
    end

    # Check expected status

    expect(unsubmitted_ids.to_set).to match_array [r4_timed_out_ok, r5_not_timed_out_ok].map(&:id)

    expect(draft_ids.to_set).to match_array [r1a_start_date_in_past,
                                             r1b_invalid_reservation_advance_days,
                                             r1c_max_visits_count_reached_at_pickup,
                                             r1c_max_visits_count_reached_at_return,
                                             r1d_no_access_to_pool,
                                             r1e_holiday,
                                             r1f_no_workday,
                                             r1g_max_reservation_time,
                                             r2_not_timed_out_with_invalid_avail_1,
                                             r3_timed_out_with_invalid_avail_1,
                                             r3_timed_out_with_invalid_avail_2,
                                             r6_user_is_suspended].map(&:id)
    expect(invalid_ids.to_set).to match_array [r1a_start_date_in_past,
                                               r1b_invalid_reservation_advance_days,
                                               r1c_max_visits_count_reached_at_pickup,
                                               r1c_max_visits_count_reached_at_return,
                                               r1d_no_access_to_pool,
                                               r1e_holiday,
                                               r1f_no_workday,
                                               r1g_max_reservation_time,
                                               r2_not_timed_out_with_invalid_avail_1,
                                               r3_timed_out_with_invalid_avail_1,
                                               r3_timed_out_with_invalid_avail_2,
                                               r6_user_is_suspended].map(&:id)

    expect(valid_until.try(:utc).change(usec: 0, sec: 0)).to eq (now.utc + 30.minutes).change(usec: 0, sec: 0)
  end
end

def dev_take_screenshots_of_each_order_panel
  dir = "tmp/dev-screenshots/invalid-reservations/"

  find_ui_list_cards.each.with_index do |line, index|
    title = line.find(".mb-1").text
    line.click

    wait_until { page.has_no_text? "LOADING" }
    sleep 0.2

    take_screenshot(dir, "#{(index + 1).to_s.rjust(2, "0")}_#{title}")

    # binding.pry

    click_on("Cancel")
  end
  system "open '#{dir}'"
end
