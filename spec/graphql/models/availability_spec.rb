require "spec_helper"
require_relative "../graphql_helper"
require_relative "reservation_advance_days_context"

describe "models connection" do
  before :each do
    @user = FactoryBot.create(
      :user,
      id: "4e91eb1a-6bda-4bc6-b4be-190a4f7460b3"
    )
    @user2 = FactoryBot.create(
      :user,
      id: "4f6af66e-b172-4c6e-b338-9db9af7911bb"
    )
    @inventory_pool = FactoryBot.create(
      :inventory_pool,
      id: "ab61cf01-08ce-4d9b-97d3-8dcd8360605a"
    )
    FactoryBot.create(:direct_access_right,
      inventory_pool: @inventory_pool,
      user: @user)
    FactoryBot.create(:direct_access_right,
      inventory_pool: @inventory_pool,
      user: @user2)
  end

  it "available quantites gathered from different pools" do
    m1 = FactoryBot.create(
      :leihs_model,
      id: "7efd48dc-676f-4438-9d1b-d0774b6704b7"
    )
    m2 = FactoryBot.create(
      :leihs_model,
      id: "5577cbcf-fdc4-4cfc-bdb9-435d75522c1d"
    )

    FactoryBot.create(:item,
      leihs_model: m1,
      responsible: @inventory_pool,
      is_borrowable: true)

    FactoryBot.create(:reservation,
      leihs_model: m1,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today,
      end_date: Date.tomorrow,
      status: "approved")

    2.times do
      FactoryBot.create(:item,
        leihs_model: m2,
        responsible: @inventory_pool,
        is_borrowable: true)
    end

    q = <<-GRAPHQL
      {
        models(
          orderBy: [{attribute: ID, direction: ASC}]
        ) {
          edges {
            node {
              id
              availableQuantityInDateRange(
                startDate: "#{Date.today}",
                endDate: "#{Date.tomorrow}"
              )
            }
          }
        }
      }
    GRAPHQL

    result = query(q, @user.id)
    expect_graphql_result(result, {
      models: {
        edges: [
          {node: {id: "5577cbcf-fdc4-4cfc-bdb9-435d75522c1d",
                  availableQuantityInDateRange: 2}},
          {node: {id: "7efd48dc-676f-4438-9d1b-d0774b6704b7",
                  availableQuantityInDateRange: 0}}
        ]
      }
    })
  end

  it "reservation with pickup location widens unavailability by transfer buffers" do
    @inventory_pool.update(transfer_buffer_before_pick_up: 2,
      transfer_buffer_after_drop_off: 2)

    model = FactoryBot.create(
      :leihs_model,
      id: "c4f1b3a0-0c1a-4a1a-8a1a-0c1a4a1a8a1a"
    )
    FactoryBot.create(:item,
      leihs_model: model,
      responsible: @inventory_pool,
      is_borrowable: true)

    pickup_location = FactoryBot.create(:pickup_location,
      inventory_pool: @inventory_pool)

    FactoryBot.create(:reservation,
      leihs_model: model,
      user: @user2,
      inventory_pool: @inventory_pool,
      pickup_location_id: pickup_location.id,
      start_date: Date.today + 3.days,
      end_date: Date.today + 4.days,
      status: "approved")

    q = ->(date) {
      <<-GRAPHQL
        {
          models(ids: ["#{model.id}"]) {
            edges {
              node {
                id
                availableQuantityInDateRange(
                  startDate: "#{date}",
                  endDate: "#{date}"
                )
              }
            }
          }
        }
      GRAPHQL
    }

    expect_quantity = ->(date, quantity) {
      result = query(q.call(date), @user.id)
      expect_graphql_result(result, {
        models: {
          edges: [
            {node: {id: model.id.to_s, availableQuantityInDateRange: quantity}}
          ]
        }
      })
    }

    expect_quantity.call(Date.today, 1)
    expect_quantity.call(Date.today + 1.day, 0)
    expect_quantity.call(Date.today + 4.days, 0)
    expect_quantity.call(Date.today + 6.days, 0)
    expect_quantity.call(Date.today + 7.days, 1)
  end

  it "two reservations with pickup location get combined transfer buffer between them" do
    @inventory_pool.update(transfer_buffer_before_pick_up: 2,
      transfer_buffer_after_drop_off: 2)

    model = FactoryBot.create(
      :leihs_model,
      id: "a1b1c1d1-0c1a-4a1a-8a1a-0c1a4a1a8a1a"
    )
    FactoryBot.create(:item,
      leihs_model: model,
      responsible: @inventory_pool,
      is_borrowable: true)

    pickup_location = FactoryBot.create(:pickup_location,
      inventory_pool: @inventory_pool)

    FactoryBot.create(:reservation,
      leihs_model: model,
      user: @user2,
      inventory_pool: @inventory_pool,
      pickup_location_id: pickup_location.id,
      start_date: Date.today,
      end_date: Date.today + 2.days,
      status: "approved")

    FactoryBot.create(:reservation,
      leihs_model: model,
      user: @user2,
      inventory_pool: @inventory_pool,
      pickup_location_id: pickup_location.id,
      start_date: Date.today + 7.days,
      end_date: Date.today + 9.days,
      status: "approved")

    q = ->(date) {
      <<-GRAPHQL
        {
          models(ids: ["#{model.id}"]) {
            edges {
              node {
                id
                availableQuantityInDateRange(
                  startDate: "#{date}",
                  endDate: "#{date}"
                )
              }
            }
          }
        }
      GRAPHQL
    }

    expect_quantity = ->(date, quantity) {
      result = query(q.call(date), @user.id)
      expect_graphql_result(result, {
        models: {
          edges: [
            {node: {id: model.id.to_s, availableQuantityInDateRange: quantity}}
          ]
        }
      })
    }

    # reservation A: today .. today+2, after-buffer -> unavailable through today+4
    expect_quantity.call(Date.today + 3.days, 0)
    expect_quantity.call(Date.today + 4.days, 0)
    # combined buffer gap (4 days: today+3..today+6) before B's before-buffer window starts
    expect_quantity.call(Date.today + 5.days, 0)
    expect_quantity.call(Date.today + 6.days, 0)
    # reservation B: today+7 .. today+9, before-buffer -> unavailable from today+5,
    # after-buffer -> unavailable through today+11
    expect_quantity.call(Date.today + 7.days, 0)
    expect_quantity.call(Date.today + 9.days, 0)
    expect_quantity.call(Date.today + 11.days, 0)
    expect_quantity.call(Date.today + 12.days, 1)
  end

  it "transfer buffer counts only orders-processing days, skipping closed non-processing ones" do
    @inventory_pool.update(transfer_buffer_before_pick_up: 0,
      transfer_buffer_after_drop_off: 2)

    closed_day = (Date.today + 2.days).strftime("%A").downcase
    Workday.find(inventory_pool_id: @inventory_pool.id)
      .update("#{closed_day}": false, "#{closed_day}_orders_processing": false)

    model = FactoryBot.create(
      :leihs_model,
      id: "c3d3e3f3-0c1a-4a1a-8a1a-0c1a4a1a8a1a"
    )
    FactoryBot.create(:item,
      leihs_model: model,
      responsible: @inventory_pool,
      is_borrowable: true)

    pickup_location = FactoryBot.create(:pickup_location,
      inventory_pool: @inventory_pool)

    FactoryBot.create(:reservation,
      leihs_model: model,
      user: @user2,
      inventory_pool: @inventory_pool,
      pickup_location_id: pickup_location.id,
      start_date: Date.today,
      end_date: Date.today + 1.day,
      status: "approved")

    q = ->(date) {
      <<-GRAPHQL
        {
          models(ids: ["#{model.id}"]) {
            edges {
              node {
                id
                availableQuantityInDateRange(
                  startDate: "#{date}",
                  endDate: "#{date}"
                )
              }
            }
          }
        }
      GRAPHQL
    }

    expect_quantity = ->(date, quantity) {
      result = query(q.call(date), @user.id)
      expect_graphql_result(result, {
        models: {
          edges: [
            {node: {id: model.id.to_s, availableQuantityInDateRange: quantity}}
          ]
        }
      })
    }

    # end_date today+1, after-buffer 2 orders-processing days, but today+2 is
    # closed and non-processing, so it doesn't count -> buffer extends through
    # today+4 instead of the raw-calendar today+3
    expect_quantity.call(Date.today + 2.days, 0)
    expect_quantity.call(Date.today + 3.days, 0)
    expect_quantity.call(Date.today + 4.days, 0)
    expect_quantity.call(Date.today + 5.days, 1)
  end

  it "availableQuantityInDateRange respects buffer for a prospective alternative-location booking" do
    @inventory_pool.update(transfer_buffer_before_pick_up: 2,
      transfer_buffer_after_drop_off: 2)

    model = FactoryBot.create(
      :leihs_model,
      id: "b2c2d2e2-0c1a-4a1a-8a1a-0c1a4a1a8a1a"
    )
    FactoryBot.create(:item,
      leihs_model: model,
      responsible: @inventory_pool,
      is_borrowable: true)

    pickup_location = FactoryBot.create(:pickup_location,
      inventory_pool: @inventory_pool)

    FactoryBot.create(:reservation,
      leihs_model: model,
      user: @user2,
      inventory_pool: @inventory_pool,
      pickup_location_id: pickup_location.id,
      start_date: Date.today + 1.day,
      end_date: Date.today + 3.days,
      status: "approved")

    q = ->(date, pickup_location_id) {
      <<-GRAPHQL
        {
          models(ids: ["#{model.id}"]) {
            edges {
              node {
                id
                availableQuantityInDateRange(
                  startDate: "#{date}",
                  endDate: "#{date}",
                  pickupLocationId: "#{pickup_location_id}"
                )
              }
            }
          }
        }
      GRAPHQL
    }

    expect_quantity = ->(date, pickup_location_id, quantity) {
      result = query(q.call(date, pickup_location_id), @user.id)
      expect_graphql_result(result, {
        models: {
          edges: [
            {node: {id: model.id.to_s, availableQuantityInDateRange: quantity}}
          ]
        }
      })
    }

    # existing reservation's own after-buffer already blocks through today+5
    # a NEW alternative-location booking starting today+6 would also need its
    # own 2-day before-buffer (today+4..+5), which overlaps the still-blocked
    # today+5 -> today+6 must be blocked too; earliest legal start is today+8
    expect_quantity.call(Date.today + 6.days, pickup_location.id, 0)
    expect_quantity.call(Date.today + 7.days, pickup_location.id, 0)
    expect_quantity.call(Date.today + 8.days, pickup_location.id, 1)
  end

  # Common setup for the two holiday-clustering specs below, built entirely
  # from Date.today + offset so the specs never depend on the actual
  # calendar date they happen to run on. The two "weekend" weekdays are
  # picked dynamically (whichever real weekdays offsets 4/5 fall on from
  # today) so a plain Workday closed-day pair recurs every 7 days from
  # there on, exactly like a real Sat/Sun weekend would.
  def setup_holiday_clustering_scenario
    today = Date.today
    weekend_day1 = (today + 4.days).strftime("%A").downcase
    weekend_day2 = (today + 5.days).strftime("%A").downcase

    @inventory_pool.update(transfer_buffer_before_pick_up: 2,
      transfer_buffer_after_drop_off: 3)

    Workday.find(inventory_pool_id: @inventory_pool.id)
      .update("#{weekend_day1}": false, "#{weekend_day1}_orders_processing": false,
        "#{weekend_day2}": false, "#{weekend_day2}_orders_processing": false)

    [1, 3, 6, 8, 10].each do |offset|
      FactoryBot.create(:holiday,
        inventory_pool: @inventory_pool,
        start_date: today + offset.days,
        end_date: today + offset.days,
        orders_processing: true)
    end

    model = FactoryBot.create(:leihs_model)
    2.times do
      FactoryBot.create(:item,
        leihs_model: model,
        responsible: @inventory_pool,
        is_borrowable: true)
    end

    pickup_location = FactoryBot.create(:pickup_location,
      inventory_pool: @inventory_pool)

    FactoryBot.create(:reservation,
      leihs_model: model,
      user: @user2,
      inventory_pool: @inventory_pool,
      pickup_location_id: pickup_location.id,
      start_date: today + 13.days,
      end_date: today + 14.days,
      status: "approved")

    [today, model, pickup_location]
  end

  it "a second prospective booking still needs its own before-buffer gap" do
    today, model, pickup_location = setup_holiday_clustering_scenario

    q = ->(start_date, end_date) {
      <<-GRAPHQL
        {
          models(ids: ["#{model.id}"]) {
            edges {
              node {
                id
                availableQuantityInDateRange(
                  startDate: "#{start_date}",
                  endDate: "#{end_date}",
                  pickupLocationId: "#{pickup_location.id}"
                )
              }
            }
          }
        }
      GRAPHQL
    }

    expect_quantity = ->(start_date, end_date, quantity) {
      result = query(q.call(start_date, end_date), @user.id)
      expect_graphql_result(result, {
        models: {
          edges: [
            {node: {id: model.id.to_s, availableQuantityInDateRange: quantity}}
          ]
        }
      })
    }

    # first reservation's own buffer blocks through today+17; a second
    # booking starting today+18 doesn't leave enough of its own
    # before-buffer gap (needs 2 orders-processing days, only
    # today+15..today+17 are free), so it's blocked too; today+20 leaves
    # enough gap and is free
    expect_quantity.call((today + 18.days).to_s, (today + 20.days).to_s, 0)
    expect_quantity.call((today + 20.days).to_s, (today + 22.days).to_s, 1)
  end

  it "booking calendar widens per day backward only, as a prospective start" do
    today, model, pickup_location = setup_holiday_clustering_scenario

    q = <<-GRAPHQL
        {
          models(ids: ["#{model.id}"]) {
            edges {
              node {
                id
                availability(
                  startDate: "#{today}",
                  endDate: "#{today + 22.days}",
                  inventoryPoolIds: ["#{@inventory_pool.id}"],
                  pickupLocationId: "#{pickup_location.id}"
                ) {
                  dates {
                    date
                    quantity
                  }
                }
              }
            }
          }
        }
    GRAPHQL

    result = query(q, @user.id)
    dates = result[:data][:models][:edges][0][:node][:availability][0][:dates]
    quantities = dates.map { |d| [d[:date][0, 10], d[:quantity]] }.to_h

    # today+9 .. today+12 are reduced by the reservation's own before-buffer
    # (2 orders-processing days backward from its today+13 start);
    # today+13/14 are the reservation itself; today+15 .. today+21 are
    # reduced because each of those days, checked as a prospective start for
    # a NEW alt-location booking, needs its own 2-day before-buffer, and
    # stepping back from them lands within the reservation's after-drop-off
    # tail (which itself runs through today+17: 3 orders-processing days
    # from today+14). today+22 is the first day whose own backward buffer
    # clears that tail entirely.
    expect(quantities).to eq(
      (today + 0.days).to_s => 2,
      (today + 1.days).to_s => 2, # holiday
      (today + 2.days).to_s => 2,
      (today + 3.days).to_s => 2, # holiday
      (today + 4.days).to_s => 2, # weekend
      (today + 5.days).to_s => 2, # weekend
      (today + 6.days).to_s => 2, # holiday
      (today + 7.days).to_s => 2,
      (today + 8.days).to_s => 2, # holiday
      (today + 9.days).to_s => 1, # before-buffer starts
      (today + 10.days).to_s => 1, # holiday
      (today + 11.days).to_s => 1, # weekend
      (today + 12.days).to_s => 1, # weekend
      (today + 13.days).to_s => 1, # reservation start
      (today + 14.days).to_s => 1, # reservation end
      (today + 15.days).to_s => 1,
      (today + 16.days).to_s => 1,
      (today + 17.days).to_s => 1, # after-buffer ends
      (today + 18.days).to_s => 1, # weekend, but own before-buffer reaches back into it
      (today + 19.days).to_s => 1, # weekend, but own before-buffer reaches back into it
      (today + 20.days).to_s => 1,
      (today + 21.days).to_s => 1, # last day still reaching back into the tail
      (today + 22.days).to_s => 2  # first day clear of it
    )
  end

  it "reservation without pickup location ignores transfer buffers" do
    @inventory_pool.update(transfer_buffer_before_pick_up: 2,
      transfer_buffer_after_drop_off: 2)

    model = FactoryBot.create(
      :leihs_model,
      id: "d5f2c4b1-1d2b-5b2b-9b2b-1d2b5b2b9b2b"
    )
    FactoryBot.create(:item,
      leihs_model: model,
      responsible: @inventory_pool,
      is_borrowable: true)

    FactoryBot.create(:reservation,
      leihs_model: model,
      user: @user2,
      inventory_pool: @inventory_pool,
      start_date: Date.today + 3.days,
      end_date: Date.today + 4.days,
      status: "approved")

    q = ->(date) {
      <<-GRAPHQL
        {
          models(ids: ["#{model.id}"]) {
            edges {
              node {
                id
                availableQuantityInDateRange(
                  startDate: "#{date}",
                  endDate: "#{date}"
                )
              }
            }
          }
        }
      GRAPHQL
    }

    expect_quantity = ->(date, quantity) {
      result = query(q.call(date), @user.id)
      expect_graphql_result(result, {
        models: {
          edges: [
            {node: {id: model.id.to_s, availableQuantityInDateRange: quantity}}
          ]
        }
      })
    }

    expect_quantity.call(Date.today + 1.day, 1)
    expect_quantity.call(Date.today + 3.days, 0)
    expect_quantity.call(Date.today + 4.days, 0)
    expect_quantity.call(Date.today + 5.days, 1)
  end

  context "start/end date restrictions" do
    let(:q) do
      @start ||= Date.today
      @end ||= Date.tomorrow

      <<-GRAPHQL
          {
            models(
              ids: ["#{@model.id}"]
            ) {
              edges {
                node {
                  id
                  availability(
                    startDate: "#{@start}",
                    endDate: "#{@end}",
                    inventoryPoolIds: ["#{@inventory_pool.id}"]#{@pickup_location_id ? %(,\n                    pickupLocationId: "#{@pickup_location_id}") : ""}
                  ) {
                    dates {
                      date
                      quantity
                      startDateRestrictions
                      endDateRestrictions
                    }
                  }
                }
              }
            }
          }
      GRAPHQL
    end

    context "close time" do
      before(:each) do
        @model = FactoryBot.create(
          :leihs_model,
          id: "8afe4e63-fded-4726-8808-6a097452374e"
        )
        FactoryBot.create(:item,
          leihs_model: @model,
          responsible: @inventory_pool,
          is_borrowable: true)
      end

      it "workday" do
        Workday.find(inventory_pool_id: @inventory_pool.id)
          .update("#{Date.today.strftime("%A").downcase}": false)

        result = query(q, @user.id)

        expect_graphql_result(result, {
          models: {
            edges: [
              {node: {id: @model.id.to_s,
                      availability: [{
                        dates: [
                          {date: "#{Date.today}T00:00:00Z",
                           quantity: 1,
                           startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE", "NON_WORKDAY"],
                           endDateRestrictions: ["NON_WORKDAY"]},
                          {date: "#{Date.tomorrow}T00:00:00Z",
                           quantity: 1,
                           startDateRestrictions: nil,
                           endDateRestrictions: nil}
                        ]
                      }]}}
            ]
          }
        })
      end

      it "holiday" do
        FactoryBot.create(:holiday,
          inventory_pool: @inventory_pool,
          start_date: Date.tomorrow.to_s,
          end_date: (Date.tomorrow + 1.day).to_s)

        result = query(q, @user.id)

        expect_graphql_result(result, {
          models: {
            edges: [
              {node: {id: @model.id.to_s,
                      availability: [{
                        dates: [
                          {date: "#{Date.today}T00:00:00Z",
                           quantity: 1,
                           startDateRestrictions: nil,
                           endDateRestrictions: nil},
                          {date: "#{Date.tomorrow}T00:00:00Z",
                           quantity: 1,
                           startDateRestrictions: ["HOLIDAY"],
                           endDateRestrictions: ["HOLIDAY"]}
                        ]
                      }]}}
            ]
          }
        })
      end

      context "earliest possible pick up date" do
        include_context "reservation advance days"

        it "works" do
          @end ||= Date.today + 5.days
          result = query(q, @user.id)

          expect_graphql_result(result, {
            models: {
              edges: [
                {node: {id: @model.id.to_s,
                        availability: [{
                          dates: [
                            {date: "#{Date.today}T00:00:00Z",
                             quantity: 1,
                             startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE"],
                             endDateRestrictions: nil},
                            {date: "#{Date.today + 1.day}T00:00:00Z",
                             quantity: 1,
                             startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE", "NON_WORKDAY"],
                             endDateRestrictions: ["NON_WORKDAY"]},
                            {date: "#{Date.today + 2.days}T00:00:00Z",
                             quantity: 1,
                             startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE", "NON_WORKDAY"],
                             endDateRestrictions: ["NON_WORKDAY"]},
                            {date: "#{Date.today + 3.days}T00:00:00Z",
                             quantity: 1,
                             startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE", "HOLIDAY"],
                             endDateRestrictions: ["HOLIDAY"]},
                            {date: "#{Date.today + 4.days}T00:00:00Z",
                             quantity: 1,
                             startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE", "HOLIDAY"],
                             endDateRestrictions: ["HOLIDAY"]},
                            {date: "#{Date.today + 5.days}T00:00:00Z",
                             quantity: 1,
                             startDateRestrictions: nil,
                             endDateRestrictions: nil}
                          ]
                        }]}}
              ]
            }
          })
        end
      end
    end

    context "pickup location" do
      let(:q) do
        @start ||= Date.today
        @end ||= Date.tomorrow

        <<-GRAPHQL
            {
              models(
                ids: ["#{@model.id}"]
              ) {
                edges {
                  node {
                    id
                    availability(
                      startDate: "#{@start}",
                      endDate: "#{@end}",
                      inventoryPoolIds: ["#{@inventory_pool.id}"]#{@pickup_location_id ? %(,\n                      pickupLocationId: "#{@pickup_location_id}") : ""}
                    ) {
                      earliestPossiblePickupDate
                      dates {
                        date
                        quantity
                        startDateRestrictions
                        endDateRestrictions
                      }
                    }
                  }
                }
              }
            }
        GRAPHQL
      end

      before(:each) do
        @model = FactoryBot.create(
          :leihs_model,
          id: "e6a3d5c2-2e3c-6c3c-ac3c-2e3c6c3cac3c"
        )
        FactoryBot.create(:item,
          leihs_model: @model,
          responsible: @inventory_pool,
          is_borrowable: true)
        @inventory_pool.update(borrow_reservation_advance_days: 1,
          transfer_buffer_before_pick_up: 3)
        @pickup_location_id =
          FactoryBot.create(:pickup_location, inventory_pool: @inventory_pool).id
      end

      it "extends earliest possible pickup date for alternative pickup location" do
        @end ||= Date.today + 3.days
        result = query(q, @user.id)

        expect_graphql_result(result, {
          models: {
            edges: [
              {node: {id: @model.id.to_s,
                      availability: [{
                        earliestPossiblePickupDate: "#{Date.today + 3.days}T00:00:00Z",
                        dates: [
                          {date: "#{Date.today}T00:00:00Z",
                           quantity: 1,
                           startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE"],
                           endDateRestrictions: nil},
                          {date: "#{Date.today + 1.day}T00:00:00Z",
                           quantity: 1,
                           startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE"],
                           endDateRestrictions: nil},
                          {date: "#{Date.today + 2.days}T00:00:00Z",
                           quantity: 1,
                           startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE"],
                           endDateRestrictions: nil},
                          {date: "#{Date.today + 3.days}T00:00:00Z",
                           quantity: 1,
                           startDateRestrictions: nil,
                           endDateRestrictions: nil}
                        ]
                      }]}}
            ]
          }
        })
      end

      it "booking calendar widens each day backward by its own before-buffer" do
        @inventory_pool.update(transfer_buffer_before_pick_up: 2,
          transfer_buffer_after_drop_off: 2)

        FactoryBot.create(:reservation,
          leihs_model: @model,
          user: @user2,
          inventory_pool: @inventory_pool,
          pickup_location_id: @pickup_location_id,
          start_date: Date.today + 1.day,
          end_date: Date.today + 3.days,
          status: "approved")

        # reservation's own window (start-2 clamped to today, end+2) is
        # today..today+5; today+6 and +7, checked as a prospective start,
        # step back 2 days into that window (today+4 and today+5), so stay
        # reduced; today+8 steps back to today+6, clear of it
        @start = Date.today + 6.days
        @end = Date.today + 8.days
        result = query(q, @user.id)

        expect_graphql_result(result, {
          models: {
            edges: [
              {node: {id: @model.id.to_s,
                      availability: [{
                        earliestPossiblePickupDate: "#{Date.today + 2.days}T00:00:00Z",
                        dates: [
                          {date: "#{Date.today + 6.days}T00:00:00Z",
                           quantity: 0,
                           startDateRestrictions: nil,
                           endDateRestrictions: nil},
                          {date: "#{Date.today + 7.days}T00:00:00Z",
                           quantity: 0,
                           startDateRestrictions: nil,
                           endDateRestrictions: nil},
                          {date: "#{Date.today + 8.days}T00:00:00Z",
                           quantity: 1,
                           startDateRestrictions: nil,
                           endDateRestrictions: nil}
                        ]
                      }]}}
            ]
          }
        })
      end

      it "does not apply the buffer when no pickup location is selected" do
        @pickup_location_id = nil
        @end ||= Date.today + 3.days
        result = query(q, @user.id)

        expect_graphql_result(result, {
          models: {
            edges: [
              {node: {id: @model.id.to_s,
                      availability: [{
                        earliestPossiblePickupDate: "#{Date.today + 1.day}T00:00:00Z",
                        dates: [
                          {date: "#{Date.today}T00:00:00Z",
                           quantity: 1,
                           startDateRestrictions: ["BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE"],
                           endDateRestrictions: nil},
                          {date: "#{Date.today + 1.day}T00:00:00Z",
                           quantity: 1,
                           startDateRestrictions: nil,
                           endDateRestrictions: nil},
                          {date: "#{Date.today + 2.days}T00:00:00Z",
                           quantity: 1,
                           startDateRestrictions: nil,
                           endDateRestrictions: nil},
                          {date: "#{Date.today + 3.days}T00:00:00Z",
                           quantity: 1,
                           startDateRestrictions: nil,
                           endDateRestrictions: nil}
                        ]
                      }]}}
            ]
          }
        })
      end
    end

    it "exposes pool transfer buffer settings" do
      @inventory_pool.update(transfer_buffer_before_pick_up: 2,
        transfer_buffer_after_drop_off: 4)

      q = <<-GRAPHQL
        {
          inventoryPool(id: "#{@inventory_pool.id}") {
            id
            transferBufferBeforePickUp
            transferBufferAfterDropOff
          }
        }
      GRAPHQL

      result = query(q, @user.id)
      expect_graphql_result(result, {
        inventoryPool: {
          id: @inventory_pool.id.to_s,
          transferBufferBeforePickUp: 2,
          transferBufferAfterDropOff: 4
        }
      })
    end

    it "maximum reservation duration" do
      q = ->(start_date, end_date) {
        <<-GRAPHQL
          {
            models(
              onlyAvailable: true
            ) {
              edges {
                node {
                  id
                  availableQuantityInDateRange(
                    startDate: "#{start_date}",
                    endDate: "#{end_date}"
                  )
                }
              }
            }
          }
        GRAPHQL
      }

      @model = FactoryBot.create(
        :leihs_model,
        id: "8afe4e63-fded-4726-8808-6a097452374e"
      )
      FactoryBot.create(:item,
        leihs_model: @model,
        responsible: @inventory_pool,
        is_borrowable: true)

      @inventory_pool.update(borrow_maximum_reservation_duration: 2)

      @start = Date.today
      @end = @start + 2.days
      result = query(q.call(@start, @end), @user.id)
      expect_graphql_result(result, {
        models: {
          edges: []
        }
      })

      @start = Date.today
      @end = @start + 1.days
      result = query(q.call(@start, @end), @user.id)
      expect_graphql_result(result, {
        models: {
          edges: [
            {node: {
              id: @model.id,
              availableQuantityInDateRange: 1
            }}
          ]
        }
      })
    end

    it "priorities" do
      @model = FactoryBot.create(
        :leihs_model,
        id: "da28cf22-db3e-4b9d-bfa8-199923b629cf"
      )
      2.times do
        FactoryBot.create(:item,
          leihs_model: @model,
          responsible: @inventory_pool,
          is_borrowable: true)
      end

      FactoryBot.create(:holiday,
        inventory_pool: @inventory_pool,
        start_date: Date.today.to_s,
        end_date: Date.today.to_s)

      FactoryBot.create(:reservation,
        leihs_model: @model,
        user: @user2,
        inventory_pool: @inventory_pool,
        start_date: Date.today,
        end_date: Date.tomorrow,
        status: "approved")

      @inventory_pool.update(borrow_reservation_advance_days: 1)
      Workday.find(inventory_pool_id: @inventory_pool.id)
        .update(max_visits: {"1": "1",
                             "2": "1",
                             "3": "1",
                             "4": "1",
                             "5": "1",
                             "6": "1",
                             "0": "1"})

      @start = @end = Date.today

      result = query(q, @user.id)

      expect_graphql_result(result, {
        models: {
          edges: [
            {node: {id: @model.id.to_s,
                    availability: [{
                      dates: [
                        {date: "#{Date.today}T00:00:00Z",
                         quantity: 1,
                         startDateRestrictions: ["VISITS_CAPACITY_REACHED", "BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE", "HOLIDAY"],
                         endDateRestrictions: ["VISITS_CAPACITY_REACHED", "HOLIDAY"]}
                      ]
                    }]}}
          ]
        }
      })

      database[:holidays].delete

      result = query(q, @user.id)

      expect_graphql_result(result, {
        models: {
          edges: [
            {node: {id: @model.id.to_s,
                    availability: [{
                      dates: [
                        {date: "#{Date.today}T00:00:00Z",
                         quantity: 1,
                         startDateRestrictions: ["VISITS_CAPACITY_REACHED", "BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE"],
                         endDateRestrictions: ["VISITS_CAPACITY_REACHED"]}
                      ]
                    }]}}
          ]
        }
      })

      @inventory_pool.update(borrow_reservation_advance_days: 0)

      result = query(q, @user.id)

      expect_graphql_result(result, {
        models: {
          edges: [
            {node: {id: @model.id.to_s,
                    availability: [{
                      dates: [
                        {date: "#{Date.today}T00:00:00Z",
                         quantity: 1,
                         startDateRestrictions: ["VISITS_CAPACITY_REACHED"],
                         endDateRestrictions: ["VISITS_CAPACITY_REACHED"]}
                      ]
                    }]}}
          ]
        }
      })
    end
  end
end
