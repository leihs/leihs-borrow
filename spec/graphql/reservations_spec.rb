require "spec_helper"
require_relative "graphql_helper"

describe "reservations" do
  let(:user) do
    FactoryBot.create(
      :user,
      id: "890c7116-7649-4620-aa60-ff3a814c6ca9"
    )
  end

  let(:inventory_pool_1) do
    FactoryBot.create(
      :inventory_pool,
      id: "93c17c42-50d6-4af9-aa3b-96a0aafb8011"
    )
  end

  let(:inventory_pool_2) do
    FactoryBot.create(
      :inventory_pool,
      id: "5271c14b-e6ef-4252-8d2a-cb0af9ed5a1f"
    )
  end

  let(:model_1) do
    FactoryBot.create(:leihs_model,
      id: "906ac7a7-1f1e-4367-b1f0-fa63052fbd0f")
  end

  let(:model_2) do
    FactoryBot.create(:leihs_model,
      id: "0a0feaf8-9537-4d39-b5f2-b9411778c90c")
  end

  let(:model_3) do
    FactoryBot.create(:leihs_model,
      id: "b5925821-6835-4b77-bd16-2ae280113eb6")
  end

  before(:example) do
    FactoryBot.create(:direct_access_right,
      inventory_pool: inventory_pool_1,
      user: user)
    FactoryBot.create(:direct_access_right,
      inventory_pool: inventory_pool_2,
      user: user)
  end

  context "create" do
    let(:q) do
      <<-GRAPHQL
        mutation(
          $modelId: UUID!,
          $startDate: Date!,
          $endDate: Date!,
          $quantity: Int!,
          $inventoryPoolIds: [UUID!],
          $userId: UUID!
        ) {
          createReservation(
            modelId: $modelId,
            startDate: $startDate,
            endDate: $endDate,
            quantity: $quantity,
            inventoryPoolIds: $inventoryPoolIds,
            userId: $userId
          ) {
            id
            createdAt
            updatedAt
          }
        }
      GRAPHQL
    end

    context "within one pool" do
      it "with explicit inventory pool ids" do
        model_1.add_item(
          FactoryBot.create(:item,
            is_borrowable: true,
            responsible: inventory_pool_1)
        )

        vars = {
          modelId: model_1.id,
          startDate: Date.tomorrow.strftime,
          endDate: (Date.tomorrow + 1.day).strftime,
          quantity: 1,
          inventoryPoolIds: [inventory_pool_1.id],
          userId: user.id
        }

        result = query(q, user.id, vars).deep_symbolize_keys

        reservations = result.dig(:data, :createReservation)
        expect(reservations.count).to eq 1
      end

      context "throws error" do
        it "model not reservable" do
          vars = {
            modelId: model_1.id,
            startDate: Date.tomorrow.strftime,
            endDate: (Date.tomorrow + 1.day).strftime,
            quantity: 1,
            userId: user.id
          }

          result = query(q, user.id, vars).deep_symbolize_keys
          expect(result[:data][:createReservation]).to be_nil
          expect(result[:errors].first[:message]).to eq \
            "Model either does not exist or is not reservable by the user."
        end

        it "quantity not available" do
          2.times do
            model_1.add_item(
              FactoryBot.create(:item,
                is_borrowable: true,
                responsible: inventory_pool_1)
            )
          end

          vars = {
            modelId: model_1.id,
            startDate: Date.tomorrow.strftime,
            endDate: (Date.tomorrow + 1.day).strftime,
            quantity: 3,
            inventoryPoolIds: [inventory_pool_1.id],
            userId: user.id
          }

          result = query(q, user.id, vars).deep_symbolize_keys
          expect(result[:data][:createReservation]).to be_nil
          expect(result[:errors].first[:message]).to eq \
            "Desired quantity is not available anymore."
        end
      end
    end
  end

  it "delete" do
    delegation = FactoryBot.create(:delegation)
    delegation.add_delegation_user(user)

    r1 = FactoryBot.create(:reservation, user: user)
    r2 = FactoryBot.create(:reservation, user: delegation)
    r3 = FactoryBot.create(:reservation)

    q = <<-GRAPHQL
      mutation($ids: [UUID!]) {
        deleteReservationLines(
          ids: $ids
        )
      }
    GRAPHQL

    vars = {ids: [r1.id, r2.id, r3.id]}

    result = query(q, user.id, vars).deep_symbolize_keys
    expect(result[:data][:deleteReservationLines].to_set).to eq(Set[r1.id, r2.id])
    expect(result[:errors]).to be_nil
  end
end
