require "spec_helper"
require_relative "../graphql_helper"

describe "repeatOrder" do
  let(:user) { FactoryBot.create(:user) }
  let(:inventory_pool) { FactoryBot.create(:inventory_pool) }
  let(:model) { FactoryBot.create(:leihs_model, transportable: true) }
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
      mutation($id: UUID!, $startDate: Date!, $endDate: Date!, $userId: UUID) {
        repeatOrder(
          id: $id,
          startDate: $startDate,
          endDate: $endDate,
          userId: $userId
        ) {
          id
          pickupLocation {
            id
            name
          }
        }
      }
    GRAPHQL
  end

  it "preserves pickup_location_id from the original reservation" do
    inventory_pool.update(enable_alternative_pickup_locations: true)
    location = FactoryBot.create(
      :pickup_location,
      inventory_pool: inventory_pool,
      name: "Alt Site"
    )
    customer_order = FactoryBot.create(:order, user: user, title: "Original")
    pool_order = FactoryBot.create(
      :pool_order,
      order: customer_order,
      user: user,
      inventory_pool: inventory_pool,
      state: "approved"
    )
    FactoryBot.create(
      :reservation,
      user: user,
      inventory_pool: inventory_pool,
      leihs_model: model,
      status: "approved",
      order_id: pool_order.id,
      pickup_location_id: location.id,
      start_date: Date.today - 10,
      end_date: Date.today - 8
    )

    result = query(mutation, user.id, {
      id: customer_order.id,
      startDate: Date.tomorrow.strftime,
      endDate: (Date.tomorrow + 1.day).strftime,
      userId: user.id
    })

    expect(result[:errors]).to be_nil
    reservations = result.dig(:data, :repeatOrder)
    expect(reservations.count).to eq 1
    expect(reservations.first[:pickupLocation]).to eq({
      id: location.id.to_s,
      name: "Alt Site"
    })
  end

  it "creates without pickup location when the original had none" do
    customer_order = FactoryBot.create(:order, user: user, title: "Original Main")
    pool_order = FactoryBot.create(
      :pool_order,
      order: customer_order,
      user: user,
      inventory_pool: inventory_pool,
      state: "approved"
    )
    FactoryBot.create(
      :reservation,
      user: user,
      inventory_pool: inventory_pool,
      leihs_model: model,
      status: "approved",
      order_id: pool_order.id,
      start_date: Date.today - 10,
      end_date: Date.today - 8
    )

    result = query(mutation, user.id, {
      id: customer_order.id,
      startDate: Date.tomorrow.strftime,
      endDate: (Date.tomorrow + 1.day).strftime,
      userId: user.id
    })

    expect(result[:errors]).to be_nil
    reservations = result.dig(:data, :repeatOrder)
    expect(reservations.count).to eq 1
    expect(reservations.first[:pickupLocation]).to be_nil
  end
end
