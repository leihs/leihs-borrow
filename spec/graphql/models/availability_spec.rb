require 'spec_helper'
require_relative '../graphql_helper'

describe 'models connection' do
  before :each do
    @user = FactoryBot.create(
      :user,
      id: '4e91eb1a-6bda-4bc6-b4be-190a4f7460b3'
    )
    @inventory_pool = FactoryBot.create(
      :inventory_pool,
      id: 'ab61cf01-08ce-4d9b-97d3-8dcd8360605a'
    )
    FactoryBot.create(:access_right,
                      inventory_pool: @inventory_pool,
                      user: @user)

  end

  it 'available quantites gathered from different pools' do
    FactoryBot.create(
      :leihs_model,
      id: '7efd48dc-676f-4438-9d1b-d0774b6704b7'
    )
    FactoryBot.create(
      :leihs_model,
      id: '5577cbcf-fdc4-4cfc-bdb9-435d75522c1d'
    )
    FactoryBot.create(
      :leihs_model,
      id: '87420e5a-c916-42f6-94ac-dd31ea32afb2'
    )

    LeihsModel.all.map do |model|
      FactoryBot.create(:item,
                        leihs_model: model,
                        responsible: @inventory_pool,
                        is_borrowable: true)
    end

    inventory_pool = FactoryBot.create(
      :inventory_pool,
      id: '6ce92dd1-cf47-4942-97a1-6bc5b495b425'
    )
    FactoryBot.create(:access_right,
                      inventory_pool: inventory_pool,
                      user: @user)

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
          { node: { id: '5577cbcf-fdc4-4cfc-bdb9-435d75522c1d',
                    availableQuantityInDateRange: 2 } },
          { node: { id: '7efd48dc-676f-4438-9d1b-d0774b6704b7',
                    availableQuantityInDateRange: 0 } },
          { node: { id: '87420e5a-c916-42f6-94ac-dd31ea32afb2',
                    availableQuantityInDateRange: 0 } }
        ]
      }
    })
  end

  context 'available quantity of 0' do
    it 'for start date in the past' do
      model = FactoryBot.create(
        :leihs_model,
        id: '948ee4ef-b576-4256-996f-38f25030f151'
      )
      FactoryBot.create(:item,
                        leihs_model: model,
                        responsible: @inventory_pool,
                        is_borrowable: true)

      q = <<-GRAPHQL
        {
          models(
            ids: ["948ee4ef-b576-4256-996f-38f25030f151"]
          ) {
            edges {
              node {
                id
                availableQuantityInDateRange(
                  startDate: "#{Date.yesterday}",
                  endDate: "#{Date.tomorrow}"
                )
                availability(
                  startDate: "#{Date.yesterday}",
                  endDate: "#{Date.tomorrow}",
                  inventoryPoolIds: ["#{@inventory_pool.id}"]
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
      expect_graphql_result(result, {
        models: {
          edges: [
            { node: { id: '948ee4ef-b576-4256-996f-38f25030f151',
                      availableQuantityInDateRange: 0,
                      availability: [
                        { dates: [
                          { date: "#{Date.yesterday}",
                            quantity: 0 },
                          { date: "#{Date.today}",
                            quantity: 1 },
                          { date: "#{Date.tomorrow}",
                            quantity: 1 } ] } ] } } ] }
      })
    end

    it 'for dates before earliest possible pick up date' do
      model = FactoryBot.create(
        :leihs_model,
        id: 'a95259db-b3bc-4324-907f-c4a5811cf049'
      )
      FactoryBot.create(:item,
                        leihs_model: model,
                        responsible: @inventory_pool,
                        is_borrowable: true)

      Workday.find(inventory_pool_id: @inventory_pool.id).update(reservation_advance_days: 2)

      q = <<-GRAPHQL
        {
          models(
            ids: ["a95259db-b3bc-4324-907f-c4a5811cf049"]
          ) {
            edges {
              node {
                id
                availability(
                  startDate: "#{Date.today}",
                  endDate: "#{Date.today + 2.days}",
                  inventoryPoolIds: ["#{@inventory_pool.id}"]
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
      expect_graphql_result(result, {
        models: {
          edges: [
            { node: { id: 'a95259db-b3bc-4324-907f-c4a5811cf049',
                      availability: [ {
                        dates: [
                          { date: "#{Date.today}",
                            quantity: 0 },
                          { date: "#{Date.today + 1.day}",
                            quantity: 0 },
                          { date: "#{Date.today + 2.days}",
                            quantity: 1 } ] } ] } } ] }
      })
    end
  end

  context 'start/end date restrictions' do
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

    context 'close time' do
      before(:each) do
        @model = FactoryBot.create(
          :leihs_model,
          id: '8afe4e63-fded-4726-8808-6a097452374e'
        )
        FactoryBot.create(:item,
                          leihs_model: @model,
                          responsible: @inventory_pool,
                          is_borrowable: true)
      end

      it 'workday' do
        Workday.find(inventory_pool_id: @inventory_pool.id)
          .update("#{Date.today.strftime("%A").downcase}": false)

        result = query(q, @user.id)

        expect_graphql_result(result, {
          models: {
            edges: [
              { node: { id: "#{@model.id}",
                        availability: [ {
                          dates: [
                            { date: "#{Date.today}",
                              quantity: 1,
                              startDateRestriction: "CLOSE_TIME",
                              endDateRestriction: "CLOSE_TIME" },
                            { date: "#{Date.tomorrow}",
                              quantity: 1,
                              startDateRestriction: nil,
                              endDateRestriction: nil } ] } ] } } ] }
        })
      end

      it 'holiday' do
        FactoryBot.create(:holiday,
                          inventory_pool: @inventory_pool,
                          start_date: "#{Date.tomorrow}",
                          end_date: "#{Date.tomorrow + 1.day}")

        result = query(q, @user.id)

        expect_graphql_result(result, {
          models: {
            edges: [
              { node: { id: "#{@model.id}",
                        availability: [ {
                          dates: [
                            { date: "#{Date.today}",
                              quantity: 1,
                              startDateRestriction: nil,
                              endDateRestriction: nil },
                            { date: "#{Date.tomorrow}",
                              quantity: 1,
                              startDateRestriction: "CLOSE_TIME",
                              endDateRestriction: "CLOSE_TIME" } ] } ] } } ] }
        })
      end
    end
    
    it 'priorities' do
      @model = FactoryBot.create(
        :leihs_model,
        id: 'da28cf22-db3e-4b9d-bfa8-199923b629cf'
      )
      FactoryBot.create(:item,
                        leihs_model: @model,
                        responsible: @inventory_pool,
                        is_borrowable: true)

      FactoryBot.create(:holiday,
                        inventory_pool: @inventory_pool,
                        start_date: "#{Date.today}",
                        end_date: "#{Date.today}")

      Workday.find(inventory_pool_id: @inventory_pool.id)
        .update(reservation_advance_days: 1,
                max_visits: {"1": "1",
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
            { node: { id: "#{@model.id}",
                      availability: [ {
                        dates: [
                          { date: "#{Date.today}",
                            quantity: 0, # because of reservation_advance_days
                            startDateRestriction: "CLOSE_TIME",
                            endDateRestriction: "CLOSE_TIME" } ] } ] } } ] }
      })

      database[:holidays].delete

      result = query(q, @user.id)

      expect_graphql_result(result, {
        models: {
          edges: [
            { node: { id: "#{@model.id}",
                      availability: [ {
                        dates: [
                          { date: "#{Date.today}",
                            quantity: 0, # because of reservation_advance_days
                            startDateRestriction: "BEFORE_EARLIEST_POSSIBLE_PICK_UP_DATE",
                            endDateRestriction: "VISITS_CAPACITY_REACHED" } ] } ] } } ] }
      })

      Workday.find(inventory_pool_id: @inventory_pool.id).update(reservation_advance_days: 0)

      result = query(q, @user.id)

      expect_graphql_result(result, {
        models: {
          edges: [
            { node: { id: "#{@model.id}",
                      availability: [ {
                        dates: [
                          { date: "#{Date.today}",
                            quantity: 1, # because of reservation_advance_days
                            startDateRestriction: "VISITS_CAPACITY_REACHED",
                            endDateRestriction: "VISITS_CAPACITY_REACHED" } ] } ] } } ] }
      })
    end
  end
end
