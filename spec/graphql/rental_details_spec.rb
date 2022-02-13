require "spec_helper"
require_relative "graphql_helper"

# {:IN_APPROVAL "submitted"
#  :REJECTED "rejected"
#  :CANCELED "canceled"
#  :RETURNED "closed"
#  :TO_PICKUP "approved"
#  :TO_RETURN "signed"})
#  + EXPIRED
#  + OVERDUE

describe "rental details" do
  before :example do
    Settings.first.update(deliver_received_order_notifications: true)
    SystemAndSecuritySettings.first.update(external_base_url: LEIHS_BORROW_HTTP_BASE_URL)
  end

  let(:user) do
    FactoryBot.create(
      :user,
      id: "b91e250f-dd02-444c-9f19-5b79312009c3",
    )
  end

  let(:inventory_pool_1) do
    FactoryBot.create(
      :inventory_pool,
      id: "96eb135e-4597-4e3f-be4d-bb7292ebf0ef",
    )
  end

  let(:inventory_pool_2) do
    FactoryBot.create(
      :inventory_pool,
      id: "605c464b-23b9-42ff-a8b5-97abf7e8cf9b",
    )
  end

  let(:inventory_pool_3) do
    FactoryBot.create(
      :inventory_pool,
      id: "4c8a8edf-b7ca-4d61-873a-72a756ab084e",
    )
  end

  let(:inventory_pool_4) do
    FactoryBot.create(
      :inventory_pool,
      id: "ca214a4f-b63a-4b76-b89c-df73efbe5e4d",
    )
  end

  let(:inventory_pool_5) do
    FactoryBot.create(
      :inventory_pool,
      id: "1b8cfaca-aaf6-4087-8113-e4c0ffd7ff88",
    )
  end

  let(:inventory_pool_6) do
    FactoryBot.create(
      :inventory_pool,
      id: "add29ab5-702f-4bb1-979d-795ae360fdff",
    )
  end

  let(:inventory_pool_7) do
    FactoryBot.create(
      :inventory_pool,
      id: "45df6bf0-c585-48e2-9c6b-04ffbd871b6b",
    )
  end

  let(:inventory_pool_8) do
    FactoryBot.create(
      :inventory_pool,
      id: "ca611668-f79e-4b39-a7c1-63f0305ebe17",
    )
  end

  let(:model_1) do
    FactoryBot.create(:leihs_model,
                      id: "8f183914-2b00-45f2-b873-2e236f5c855f")
  end

  let(:model_2) do
    FactoryBot.create(:leihs_model,
                      id: "8b13bf0d-f89f-4f8c-b6ad-ccf06829c6f0")
  end

  let(:model_3) do
    FactoryBot.create(:leihs_model,
                      id: "93a36af3-f630-452b-884b-0f5936119321")
  end

  let(:model_4) do
    FactoryBot.create(:leihs_model,
                      id: "d36addb6-e374-4a4f-9b0f-6c6c3db5124e")
  end

  let(:model_5) do
    FactoryBot.create(:leihs_model,
                      id: "18613bdd-c410-4f88-bd41-69ae1aa2ef73")
  end

  let(:model_6) do
    FactoryBot.create(:leihs_model,
                      id: "91d29af6-291d-46df-96b5-d065e8268ed6")
  end

  let(:model_7) do
    FactoryBot.create(:leihs_model,
                      id: "c162b535-25d3-48b8-9759-786423be5a3c")
  end

  let(:model_8) do
    FactoryBot.create(:leihs_model,
                      id: "54bac19e-9b4f-4379-9b8b-167c287670a0")
  end

  let(:order) do
    FactoryBot.create(:order, user: user)
  end

  let(:in_approval_pool_order) do
    po
  end

  before(:example) do
    [inventory_pool_1,
     inventory_pool_2,
     inventory_pool_3,
     inventory_pool_4,
     inventory_pool_5,
     inventory_pool_6,
     inventory_pool_7,
     inventory_pool_8].each do |ip|
       FactoryBot.create(:direct_access_right, inventory_pool: ip, user: user)
     end

     [model_1,
      model_2,
      model_3,
      model_4,
      model_5,
      model_6,
      model_7,
      model_8].each_with_index do |m, i|
        3.times do
          FactoryBot.create(:item,
                            owner: method("inventory_pool_#{i.succ}").call,
                            responsible: method("inventory_pool_#{i.succ}").call,
                            leihs_model: m,
                            is_borrowable: true)
        end
      end
  end

  context "complex rental" do
    let(:q) do
      <<~GQL
        query customerOrderShow($id: UUID!) {
          rental(id: $id) {
            id
            purpose
            title
            isCustomerOrder
            state: rentalState
            refinedRentalState
            fromDate
            untilDate
            totalDays
            totalQuantity
            rejectedQuantity
            expiredUnapprovedQuantity
            expiredQuantity
            overdueQuantity
            approveFulfillment {
              fulfilledQuantity
              toFulfillQuantity
            }
            pickupFulfillment {
              fulfilledQuantity
              toFulfillQuantity
            }
            returnFulfillment {
              fulfilledQuantity
              toFulfillQuantity
            }
            subOrdersByPool {
              id
              inventoryPool {
                ...poolProps
              }
              state
              rejectedReason
              reservations {
                ...reservationProps
              }
              order {
                id
                title
                purpose
              }
              contracts {
                ...contractsConnectionProps
              }
              createdAt
              updatedAt
            }
            user {
              id
            }
            contracts {
              ...contractsConnectionProps
            }
            reservations {
              ...reservationProps
            }
            createdAt
            updatedAt
          }
        }

        fragment poolProps on InventoryPool {
          id
          # description
          # isActive
          # email
          name
          # shortname
          # hasReservableItems
          # maximumReservationTime
        }

        fragment reservationProps on Reservation {
          id
          status
          startDate
          endDate
          model {
            id
            name
          }
          option {
            id
            name
          }
          quantity
          inventoryPool {
            id
            name
          }
        }

        fragment contractsConnectionProps on ContractsConnection {
          edges {
            node {
              id
              compactId
              printUrl
              createdAt
              # note
              # reservations {
              #   ...reservationProps
              # }
            }
          }
        }
      GQL
    end

    it "index" do
      in_approval_pool_order = FactoryBot.create(:pool_order,
                                                 order: order,
                                                 user: user,
                                                 inventory_pool: inventory_pool_1,
                                                 state: "submitted")
      in_approval_reservation = FactoryBot.create(:reservation,
                                                  user: user,
                                                  order: in_approval_pool_order,
                                                  inventory_pool: inventory_pool_1,
                                                  status: "submitted",
                                                  leihs_model: model_1)

      rejected_pool_order = FactoryBot.create(:pool_order,
                                              order: order,
                                              user: user,
                                              inventory_pool: inventory_pool_2,
                                              state: "rejected")
      rejected_reservation = FactoryBot.create(:reservation,
                                               user: user,
                                               order: rejected_pool_order,
                                               inventory_pool: inventory_pool_2,
                                               status: "rejected",
                                               leihs_model: model_2)

      canceled_pool_order = FactoryBot.create(:pool_order,
                                              order: order,
                                              user: user,
                                              inventory_pool: inventory_pool_3,
                                              state: "canceled")
      canceled_reservation = FactoryBot.create(:reservation,
                                               user: user,
                                               order: canceled_pool_order,
                                               inventory_pool: inventory_pool_3,
                                               status: "canceled",
                                               leihs_model: model_3)

      approved_pool_order_1 = FactoryBot.create(:pool_order,
                                                order: order,
                                                user: user,
                                                inventory_pool: inventory_pool_4,
                                                state: "approved")

      closed_contract = Contract.create_with_disabled_triggers(
        "b53dfcf8-bf18-43a4-9897-5654f1b8e095",
        user.id,
        inventory_pool_4.id,
        "closed"
      )

      returned_reservation = FactoryBot.create(:reservation,
                                               user: user,
                                               inventory_pool: inventory_pool_4,
                                               contract: closed_contract,
                                               order: approved_pool_order_1,
                                               status: "closed",
                                               leihs_model: model_4,
                                               item: model_4.items.first)

      approved_pool_order_2 = FactoryBot.create(:pool_order,
                                                order: order,
                                                user: user,
                                                inventory_pool: inventory_pool_5,
                                                state: "approved")

      approved_reservation = FactoryBot.create(:reservation,
                                               user: user,
                                               inventory_pool: inventory_pool_5,
                                               order: approved_pool_order_2,
                                               status: "approved",
                                               leihs_model: model_5)

      approved_pool_order_3 = FactoryBot.create(:pool_order,
                                                order: order,
                                                user: user,
                                                inventory_pool: inventory_pool_6,
                                                state: "approved")

      open_contract = Contract.create_with_disabled_triggers(
        "da745242-b120-45ca-81f8-efb8d38a1953",
        user.id,
        inventory_pool_6.id,
        "open"
      )

      to_return_reservation = FactoryBot.create(:reservation,
                                                user: user,
                                                inventory_pool: inventory_pool_6,
                                                contract: open_contract,
                                                order: approved_pool_order_3,
                                                status: "signed",
                                                leihs_model: model_6,
                                                item: model_6.items.first)

      expired_unapproved_order = FactoryBot.create(:pool_order,
                                                   order: order,
                                                   user: user,
                                                   inventory_pool: inventory_pool_7,
                                                   state: "submitted")
      expired_unapproved_reservation = FactoryBot.create(:reservation,
                                                         user: user,
                                                         inventory_pool: inventory_pool_7,
                                                         order: expired_unapproved_order,
                                                         status: "submitted",
                                                         start_date: Date.yesterday - 1.day,
                                                         end_date: Date.yesterday,
                                                         leihs_model: model_7)

      expired_order = FactoryBot.create(:pool_order,
                                        order: order,
                                        user: user,
                                        inventory_pool: inventory_pool_7,
                                        state: "approved")
      expired_reservation = FactoryBot.create(:reservation,
                                              user: user,
                                              inventory_pool: inventory_pool_7,
                                              order: expired_order,
                                              status: "approved",
                                              start_date: Date.yesterday - 1.day,
                                              end_date: Date.yesterday,
                                              leihs_model: model_7)

      approved_pool_order_4 = FactoryBot.create(:pool_order,
                                                order: order,
                                                user: user,
                                                inventory_pool: inventory_pool_8,
                                                state: "approved")

      open_contract_2 = Contract.create_with_disabled_triggers(
        "e7aa882d-9beb-43cc-94b0-be40fea2cbbb",
        user.id,
        inventory_pool_8.id,
        "open"
      )

      returned_reservation = FactoryBot.create(:reservation,
                                               user: user,
                                               inventory_pool: inventory_pool_8,
                                               contract: open_contract_2,
                                               order: approved_pool_order_4,
                                               status: "signed",
                                               start_date: Date.today - 2.days,
                                               end_date: Date.yesterday,
                                               leihs_model: model_8,
                                               item: model_8.items.first)

      vars = { id: order.id }
      result = query(q, user.id, vars).deep_symbolize_keys
      rental = result.dig(:data, :rental)

      expect(rental[:refinedRentalState].to_set).to eq ["IN_APPROVAL",
                                                        "REJECTED",
                                                        "CANCELED",
                                                        "RETURNED",
                                                        "TO_PICKUP",
                                                        "TO_RETURN",
                                                        "EXPIRED",
                                                        "OVERDUE"].to_set
      expect(rental).to include({
        totalQuantity: 9,
        rejectedQuantity: 1,
        expiredUnapprovedQuantity: 1,
        expiredQuantity: 1,
        overdueQuantity: 1,
        approveFulfillment: { fulfilledQuantity: 5, toFulfillQuantity: 8 },
        pickupFulfillment: { fulfilledQuantity: 3, toFulfillQuantity: 8 },
        returnFulfillment: { fulfilledQuantity: 1, toFulfillQuantity: 8 },
      })
    end
  end

  # context "customer order with additional hand over reservation" do
  #   let(:q) do
  #     <<~GQL
  #       query customerOrderShow($id: UUID!) {
  #         rental(id: $id) {
  #           id
  #           reservations {
  #             id
  #           }
  #         }
  #       }
  #     GQL
  #   end

  #   example "works" do
  #     pool_order = FactoryBot.create(:pool_order,
  #                                    order: order,
  #                                    user: user,
  #                                    inventory_pool: inventory_pool_1,
  #                                    state: "approved")

  #     open_contract = Contract.create_with_disabled_triggers(
  #       "f3051104-e493-4786-af5e-1473b71981ff",
  #       user.id,
  #       inventory_pool_1.id,
  #       "open"
  #     )

  #     open_contract_2 = Contract.create_with_disabled_triggers(
  #       "58af2065-0bae-4780-bb2c-dce7e11372ef",
  #       user.id,
  #       inventory_pool_1.id,
  #       "open"
  #     )

  #     signed_reservation_1 = FactoryBot.create(:reservation,
  #                                              user: user,
  #                                              order: pool_order,
  #                                              contract: open_contract,
  #                                              inventory_pool: inventory_pool_1,
  #                                              status: "signed",
  #                                              leihs_model: model_1,
  #                                              item: model_1.items.first)

  #     signed_reservation_2 = FactoryBot.create(:reservation,
  #                                              user: user,
  #                                              contract: open_contract,
  #                                              inventory_pool: inventory_pool_1,
  #                                              status: "signed",
  #                                              leihs_model: model_2,
  #                                              item: model_2.items.first)

  #     signed_reservation_3 = FactoryBot.create(:reservation,
  #                                              user: user,
  #                                              contract: open_contract_2,
  #                                              inventory_pool: inventory_pool_1,
  #                                              status: "signed",
  #                                              leihs_model: model_4,
  #                                              item: model_4.items.first)

  #     approved_reservation = FactoryBot.create(:reservation,
  #                                              user: user,
  #                                              inventory_pool: inventory_pool_1,
  #                                              status: "approved",
  #                                              leihs_model: model_3)

  #     vars = { id: order.id }
  #     result = query(q, user.id, vars).deep_symbolize_keys
  #     r_ids = result.dig(:data, :rental, :reservations).map { |r| r[:id] }
  #     expect(r_ids.to_set).to eq [signed_reservation_1.id, signed_reservation_2.id].to_set
  #   end
  # end

  context "expired reservations considered as closed" do
    let(:q) do
      <<~GQL
        query customerOrderShow($id: UUID!) {
          rental(id: $id) {
            id
            rentalState
          }
        }
      GQL
    end

    example "works" do
      pool_order = FactoryBot.create(:pool_order,
                                     order: order,
                                     user: user,
                                     inventory_pool: inventory_pool_1,
                                     state: "submitted")
      pool_order_2 = FactoryBot.create(:pool_order,
                                       order: order,
                                       user: user,
                                       inventory_pool: inventory_pool_2,
                                       state: "approved")
      pool_order_3 = FactoryBot.create(:pool_order,
                                       order: order,
                                       user: user,
                                       inventory_pool: inventory_pool_3,
                                       state: "rejected")
      pool_order_4 = FactoryBot.create(:pool_order,
                                       order: order,
                                       user: user,
                                       inventory_pool: inventory_pool_4,
                                       state: "canceled")

      closed_contract = Contract.create_with_disabled_triggers(
        "06eebe76-d5c6-469e-a51e-73858bf99c60",
        user.id,
        inventory_pool_5.id,
        "closed"
      )

      submitted_reservation = FactoryBot.create(:reservation,
                                                user: user,
                                                inventory_pool: inventory_pool_1,
                                                start_date: Date.yesterday - 1.day,
                                                end_date: Date.yesterday,
                                                order: pool_order,
                                                status: "submitted",
                                                leihs_model: model_1)
      approved_reservation = FactoryBot.create(:reservation,
                                               user: user,
                                               inventory_pool: inventory_pool_2,
                                               start_date: Date.yesterday - 1.day,
                                               end_date: Date.yesterday,
                                               order: pool_order_2,
                                               status: "approved",
                                               leihs_model: model_2)
      rejected_reservation = FactoryBot.create(:reservation,
                                               user: user,
                                               inventory_pool: inventory_pool_3,
                                               order: pool_order_3,
                                               status: "rejected",
                                               leihs_model: model_3)
      canceled_reservation = FactoryBot.create(:reservation,
                                               user: user,
                                               inventory_pool: inventory_pool_4,
                                               order: pool_order_4,
                                               status: "canceled",
                                               leihs_model: model_4)
      closed_reservation = FactoryBot.create(:reservation,
                                             user: user,
                                             inventory_pool: inventory_pool_5,
                                             status: "closed",
                                             contract: closed_contract,
                                             leihs_model: model_5)

      vars = { id: order.id }
      result = query(q, user.id, vars).deep_symbolize_keys
      rental_state = result.dig(:data, :rental, :rentalState)
      expect(rental_state).to eq "CLOSED"

      submitted_reservation = FactoryBot.create(:reservation,
                                                user: user,
                                                inventory_pool: inventory_pool_1,
                                                start_date: Date.yesterday - 1.day,
                                                end_date: Date.today,
                                                order: pool_order,
                                                status: "submitted",
                                                leihs_model: model_1)

      vars = { id: order.id }
      result = query(q, user.id, vars).deep_symbolize_keys
      rental_state = result.dig(:data, :rental, :rentalState)
      expect(rental_state).to eq "OPEN"
    end
  end
end
