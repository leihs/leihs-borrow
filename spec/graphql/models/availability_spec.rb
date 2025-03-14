require "spec_helper"
require_relative "../graphql_helper"

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
                    inventoryPoolIds: ["#{@inventory_pool.id}"]
                  ) {
                    dates {
                      date
                      quantity
                      startDateRestriction
                      endDateRestriction
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
                           startDateRestriction: "CLOSE_TIME",
                           endDateRestriction: "CLOSE_TIME"},
                          {date: "#{Date.tomorrow}T00:00:00Z",
                           quantity: 1,
                           startDateRestriction: nil,
                           endDateRestriction: nil}
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
                           startDateRestriction: nil,
                           endDateRestriction: nil},
                          {date: "#{Date.tomorrow}T00:00:00Z",
                           quantity: 1,
                           startDateRestriction: "CLOSE_TIME",
                           endDateRestriction: "CLOSE_TIME"}
                        ]
                      }]}}
            ]
          }
        })
      end

      it "earliest possible pick up date" do
        days_of_week = [:sunday,
          :monday,
          :tuesday,
          :wednesday,
          :thursday,
          :friday,
          :saturday].cycle

        # Update the workday for the next day to be closed.
        closed_date = Date.today + 1.day
        closed_day =
          days_of_week
            .with_index
            .detect { |_, idx| idx == closed_date.wday }
            .first

        @inventory_pool.workday.update(closed_day => false)

        # Add holiday starting 2 days after the closed_date and lasting for 2 days.
        FactoryBot.create(:holiday,
          start_date: (closed_date + 2.day).to_s,
          end_date: (closed_date + 3.day).to_s,
          inventory_pool_id: @inventory_pool.id)

        @inventory_pool.update(borrow_reservation_advance_days: 3)

        @end ||= Date.today + 6.days
        result = query(q, @user.id)

        expect_graphql_result(result, {
          models: {
            edges: [
              {node: {id: @model.id.to_s,
                      availability: [{
                        dates: [
                          {date: "#{Date.today}T00:00:00Z",
                           quantity: 1,
                           startDateRestriction: "BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE",
                           endDateRestriction: nil},
                          {date: "#{Date.today + 1.day}T00:00:00Z",
                           quantity: 1,
                           startDateRestriction: "CLOSE_TIME",
                           endDateRestriction: "CLOSE_TIME"},
                          {date: "#{Date.today + 2.days}T00:00:00Z",
                           quantity: 1,
                           startDateRestriction: "BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE",
                           endDateRestriction: nil},
                          {date: "#{Date.today + 3.days}T00:00:00Z",
                           quantity: 1,
                           startDateRestriction: "CLOSE_TIME",
                           endDateRestriction: "CLOSE_TIME"},
                          {date: "#{Date.today + 4.days}T00:00:00Z",
                           quantity: 1,
                           startDateRestriction: "CLOSE_TIME",
                           endDateRestriction: "CLOSE_TIME"},
                          {date: "#{Date.today + 5.days}T00:00:00Z",
                           quantity: 1,
                           startDateRestriction: "BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE",
                           endDateRestriction: nil},
                          {date: "#{Date.today + 6.days}T00:00:00Z",
                           quantity: 1,
                           startDateRestriction: nil,
                           endDateRestriction: nil}
                        ]
                      }]}}
            ]
          }
        })
      end
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
                         startDateRestriction: "CLOSE_TIME",
                         endDateRestriction: "CLOSE_TIME"}
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
                         startDateRestriction: "BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE",
                         endDateRestriction: "VISITS_CAPACITY_REACHED"}
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
                         startDateRestriction: "VISITS_CAPACITY_REACHED",
                         endDateRestriction: "VISITS_CAPACITY_REACHED"}
                      ]
                    }]}}
          ]
        }
      })
    end
  end
end
