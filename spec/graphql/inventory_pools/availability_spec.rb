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

  let(:q) do
    <<-GRAPHQL
      {
        inventoryPools(ids: ["#{@inventory_pool.id}"]) {
          id
          defaultPickupLocationName
          enableAlternativePickupLocations
          pickupLocations {
            id
            name
          }
          availability(
            startDate: "#{Date.today}",
            endDate: "#{Date.today + 3.days}"
          ) {
            earliestPossiblePickupDate
            earliestPossiblePickupDateForAltLocations
            dates {
              date
              startDateRestrictions
              endDateRestrictions
            }
            datesForAltLocations {
              date
              startDateRestrictions
              endDateRestrictions
            }
          }
        }
      }
    GRAPHQL
  end

  context "when pool has a pickup location" do
    let!(:pickup_location) do
      FactoryBot.create(:pickup_location,
        inventory_pool: @inventory_pool,
        name: "Alt Site")
    end

    before(:each) do
      @inventory_pool.update(enable_alternative_pickup_locations: true)
    end

    it "returns pickup locations and dual date series (main + alt)" do
      result = query(q, @user.id)

      expect_graphql_result(result, {
        inventoryPools: [{
          id: @inventory_pool.id.to_s,
          defaultPickupLocationName: @inventory_pool.default_pickup_location_name,
          enableAlternativePickupLocations: true,
          pickupLocations: [
            {id: pickup_location.id.to_s, name: "Alt Site"}
          ],
          availability: {
            earliestPossiblePickupDate: "#{Date.today + 1.day}T00:00:00Z",
            earliestPossiblePickupDateForAltLocations: "#{Date.today + 3.days}T00:00:00Z",
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
            ],
            datesForAltLocations: [
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

    it "when advance days exceed buffer, both series use advance days (max)" do
      @inventory_pool.update(
        borrow_reservation_advance_days: 3,
        transfer_buffer_before_pick_up: 1
      )

      result = query(q, @user.id)

      expect_graphql_result(result, {
        inventoryPools: [{
          id: @inventory_pool.id.to_s,
          defaultPickupLocationName: @inventory_pool.default_pickup_location_name,
          enableAlternativePickupLocations: true,
          pickupLocations: [
            {id: pickup_location.id.to_s, name: "Alt Site"}
          ],
          availability: {
            earliestPossiblePickupDate: "#{Date.today + 3.days}T00:00:00Z",
            earliestPossiblePickupDateForAltLocations: "#{Date.today + 3.days}T00:00:00Z",
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
            ],
            datesForAltLocations: [
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

    context "when alternative pickup locations are disabled" do
      before(:each) do
        @inventory_pool.update(enable_alternative_pickup_locations: false)
      end

      it "datesForAltLocations is nil even though pickup locations exist" do
        result = query(q, @user.id)

        expect_graphql_result(result, {
          inventoryPools: [{
            id: @inventory_pool.id.to_s,
            defaultPickupLocationName: @inventory_pool.default_pickup_location_name,
            enableAlternativePickupLocations: false,
            pickupLocations: [],
            availability: {
              earliestPossiblePickupDate: "#{Date.today + 1.day}T00:00:00Z",
              earliestPossiblePickupDateForAltLocations: nil,
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
              ],
              datesForAltLocations: nil
            }
          }]
        })
      end
    end
  end

  context "when pool has no pickup locations" do
    it "datesForAltLocations is nil and pickupLocations is empty" do
      result = query(q, @user.id)

      expect_graphql_result(result, {
        inventoryPools: [{
          id: @inventory_pool.id.to_s,
          defaultPickupLocationName: @inventory_pool.default_pickup_location_name,
          enableAlternativePickupLocations: false,
          pickupLocations: [],
          availability: {
            earliestPossiblePickupDate: "#{Date.today + 1.day}T00:00:00Z",
            earliestPossiblePickupDateForAltLocations: nil,
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
            ],
            datesForAltLocations: nil
          }
        }]
      })
    end
  end
end
