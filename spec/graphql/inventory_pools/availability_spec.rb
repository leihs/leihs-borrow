require "spec_helper"
require_relative "../graphql_helper"

describe "inventoryPool.availability" do
  before :each do
    @user = FactoryBot.create(:user)
    @inventory_pool = FactoryBot.create(:inventory_pool)
    FactoryBot.create(:direct_access_right,
      inventory_pool: @inventory_pool,
      user: @user)
    @inventory_pool.update(borrow_reservation_advance_days: 1,
      transfer_buffer_before_pick_up: 3)
  end

  def availability_query(consider_alt:)
    <<-GRAPHQL
      {
        inventoryPools(ids: ["#{@inventory_pool.id}"]) {
          id
          availability(
            startDate: "#{Date.today}",
            endDate: "#{Date.today + 3.days}",
            considerAlternativePickupLocations: #{consider_alt}
          ) {
            earliestPossiblePickupDate
            dates {
              date
              startDateRestrictions
              endDateRestrictions
            }
          }
        }
      }
    GRAPHQL
  end

  it "without considerAlternativePickupLocations uses reservation advance days" do
    result = query(availability_query(consider_alt: false), @user.id)

    expect_graphql_result(result, {
      inventoryPools: [{
        id: @inventory_pool.id.to_s,
        availability: {
          earliestPossiblePickupDate: "#{Date.today + 1.day}T00:00:00Z",
          dates: [
            {date: "#{Date.today}T00:00:00Z",
             startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE"],
             endDateRestrictions: nil},
            {date: "#{Date.today + 1.day}T00:00:00Z",
             startDateRestrictions: nil,
             endDateRestrictions: nil},
            {date: "#{Date.today + 2.days}T00:00:00Z",
             startDateRestrictions: nil,
             endDateRestrictions: nil},
            {date: "#{Date.today + 3.days}T00:00:00Z",
             startDateRestrictions: nil,
             endDateRestrictions: nil}
          ]
        }
      }]
    })
  end

  it "with considerAlternativePickupLocations uses max(advance days, transfer buffer)" do
    result = query(availability_query(consider_alt: true), @user.id)

    expect_graphql_result(result, {
      inventoryPools: [{
        id: @inventory_pool.id.to_s,
        availability: {
          earliestPossiblePickupDate: "#{Date.today + 3.days}T00:00:00Z",
          dates: [
            {date: "#{Date.today}T00:00:00Z",
             startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE"],
             endDateRestrictions: nil},
            {date: "#{Date.today + 1.day}T00:00:00Z",
             startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE"],
             endDateRestrictions: nil},
            {date: "#{Date.today + 2.days}T00:00:00Z",
             startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE"],
             endDateRestrictions: nil},
            {date: "#{Date.today + 3.days}T00:00:00Z",
             startDateRestrictions: nil,
             endDateRestrictions: nil}
          ]
        }
      }]
    })
  end

  it "when advance days exceed buffer, considerAlternativePickupLocations still uses advance days (max)" do
    @inventory_pool.update(
      borrow_reservation_advance_days: 3,
      transfer_buffer_before_pick_up: 1
    )

    result = query(availability_query(consider_alt: true), @user.id)

    expect_graphql_result(result, {
      inventoryPools: [{
        id: @inventory_pool.id.to_s,
        availability: {
          earliestPossiblePickupDate: "#{Date.today + 3.days}T00:00:00Z",
          dates: [
            {date: "#{Date.today}T00:00:00Z",
             startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE"],
             endDateRestrictions: nil},
            {date: "#{Date.today + 1.day}T00:00:00Z",
             startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE"],
             endDateRestrictions: nil},
            {date: "#{Date.today + 2.days}T00:00:00Z",
             startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE"],
             endDateRestrictions: nil},
            {date: "#{Date.today + 3.days}T00:00:00Z",
             startDateRestrictions: nil,
             endDateRestrictions: nil}
          ]
        }
      }]
    })
  end
end
