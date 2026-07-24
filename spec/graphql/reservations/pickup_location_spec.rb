require "spec_helper"
require_relative "../graphql_helper"

describe "createReservation with pickupLocationId" do
  let(:user) { FactoryBot.create(:user) }
  let(:inventory_pool) { FactoryBot.create(:inventory_pool) }
  let(:model) { FactoryBot.create(:leihs_model) }
  let!(:access_right) do
    FactoryBot.create(:direct_access_right,
      inventory_pool: inventory_pool,
      user: user)
  end
  let!(:item) do
    model.add_item(
      FactoryBot.create(:item,
        is_borrowable: true,
        responsible: inventory_pool)
    )
  end

  let(:mutation) do
    <<-GRAPHQL
      mutation(
        $modelId: UUID!,
        $startDate: Date!,
        $endDate: Date!,
        $quantity: Int!,
        $inventoryPoolIds: [UUID!],
        $pickupLocationId: UUID,
        $userId: UUID!
      ) {
        createReservation(
          modelId: $modelId,
          startDate: $startDate,
          endDate: $endDate,
          quantity: $quantity,
          inventoryPoolIds: $inventoryPoolIds,
          pickupLocationId: $pickupLocationId,
          userId: $userId
        ) {
          id
          pickupLocation {
            id
            name
          }
          model {
            id
            transportable
          }
        }
      }
    GRAPHQL
  end

  def base_vars(extra = {})
    {
      modelId: model.id,
      startDate: Date.tomorrow.strftime,
      endDate: (Date.tomorrow + 1.day).strftime,
      quantity: 1,
      inventoryPoolIds: [inventory_pool.id],
      userId: user.id
    }.merge(extra)
  end

  it "creates reservation without pickup location (main warehouse)" do
    result = query(mutation, user.id, base_vars)
    expect(result[:errors]).to be_nil
    reservations = result.dig(:data, :createReservation)
    expect(reservations.count).to eq 1
    expect(reservations.first[:pickupLocation]).to be_nil
    expect(reservations.first[:model][:transportable]).to eq true
  end

  it "creates reservation with pickup location belonging to the pool" do
    location = FactoryBot.create(
      :pickup_location,
      inventory_pool: inventory_pool,
      name: "Alpha Site"
    )

    result = query(mutation, user.id, base_vars(pickupLocationId: location.id))
    expect(result[:errors]).to be_nil
    reservations = result.dig(:data, :createReservation)
    expect(reservations.count).to eq 1
    expect(reservations.first[:pickupLocation]).to eq(
      {id: location.id.to_s, name: "Alpha Site"}
    )
  end

  it "rejects pickup location from another pool" do
    other_pool = FactoryBot.create(:inventory_pool)
    location = FactoryBot.create(
      :pickup_location,
      inventory_pool: other_pool,
      name: "Wrong Pool Site"
    )

    result = query(mutation, user.id, base_vars(pickupLocationId: location.id))
    expect(result[:data][:createReservation]).to be_nil
    expect(result[:errors].first[:message]).to eq \
      "Pickup location does not belong to the selected inventory pool."
  end

  it "rejects alternative pickup location when model is not transportable" do
    model.update(transportable: false)
    location = FactoryBot.create(
      :pickup_location,
      inventory_pool: inventory_pool,
      name: "Alpha Site"
    )

    result = query(mutation, user.id, base_vars(pickupLocationId: location.id))
    expect(result[:data][:createReservation]).to be_nil
    expect(result[:errors].first[:message]).to eq \
      "Model is not transportable to an alternative pickup location."
  end

  it "exposes transportable on model (default true)" do
    q = <<-GRAPHQL
      {
        model(id: "#{model.id}") {
          id
          transportable
        }
      }
    GRAPHQL
    result = query(q, user.id)
    expect(result[:errors]).to be_nil
    expect(result.dig(:data, :model, :transportable)).to eq true
  end
end
