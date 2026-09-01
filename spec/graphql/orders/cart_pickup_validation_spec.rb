require "spec_helper"
require_relative "../graphql_helper"

describe "cart validation for alternative pickup locations" do
  let(:user) { FactoryBot.create(:user) }
  let(:inventory_pool) do
    FactoryBot.create(
      :inventory_pool,
      enable_alternative_pickup_locations: true,
      borrow_reservation_advance_days: 1,
      transfer_buffer_before_pick_up: 3
    )
  end
  let!(:access_right) do
    FactoryBot.create(:direct_access_right, inventory_pool: inventory_pool, user: user)
  end
  let(:location) do
    FactoryBot.create(:pickup_location, inventory_pool: inventory_pool, name: "Alt Site")
  end

  let(:cart_query) do
    <<-GRAPHQL
      {
        currentUser {
          user {
            unsubmittedOrder {
              invalidReservationIds
              reservations {
                id
              }
            }
          }
        }
      }
    GRAPHQL
  end

  def create_unsubmitted!(model:, pickup_location_id:, start_date:, end_date:)
    FactoryBot.create(
      :reservation,
      user: user,
      inventory_pool: inventory_pool,
      leihs_model: model,
      status: "unsubmitted",
      pickup_location_id: pickup_location_id,
      start_date: start_date,
      end_date: end_date
    )
  end

  it "marks non-transportable model with alt pickup as invalid" do
    model = FactoryBot.create(:leihs_model, transportable: false, product: "Non Transportable Alt")
    model.add_item(FactoryBot.create(:item, is_borrowable: true, responsible: inventory_pool))
    reservation = create_unsubmitted!(
      model: model,
      pickup_location_id: location.id,
      start_date: 5.days.from_now,
      end_date: 6.days.from_now
    )

    result = query(cart_query, user.id)
    expect(result[:errors]).to be_nil
    expect(result.dig(:data, :currentUser, :user, :unsubmittedOrder, :invalidReservationIds))
      .to include(reservation.id.to_s)
  end

  it "marks reservation when alternative pickup locations are disabled" do
    model = FactoryBot.create(:leihs_model, transportable: true, product: "Gone Location")
    model.add_item(FactoryBot.create(:item, is_borrowable: true, responsible: inventory_pool))
    reservation = create_unsubmitted!(
      model: model,
      pickup_location_id: location.id,
      start_date: 5.days.from_now,
      end_date: 6.days.from_now
    )
    inventory_pool.update(enable_alternative_pickup_locations: false)

    result = query(cart_query, user.id)
    expect(result[:errors]).to be_nil
    expect(result.dig(:data, :currentUser, :user, :unsubmittedOrder, :invalidReservationIds))
      .to include(reservation.id.to_s)
  end

  it "marks reservation when pickup location belongs to another pool" do
    model = FactoryBot.create(:leihs_model, transportable: true, product: "Wrong Pool Location")
    model.add_item(FactoryBot.create(:item, is_borrowable: true, responsible: inventory_pool))
    reservation = create_unsubmitted!(
      model: model,
      pickup_location_id: location.id,
      start_date: 5.days.from_now,
      end_date: 6.days.from_now
    )
    other_pool = FactoryBot.create(:inventory_pool, enable_alternative_pickup_locations: true)
    FactoryBot.create(:direct_access_right, inventory_pool: other_pool, user: user)
    location.update(inventory_pool: other_pool)

    result = query(cart_query, user.id)
    expect(result[:errors]).to be_nil
    expect(result.dig(:data, :currentUser, :user, :unsubmittedOrder, :invalidReservationIds))
      .to include(reservation.id.to_s)
  end

  it "marks plain pickup when advance days are violated" do
    model = FactoryBot.create(:leihs_model, transportable: true, product: "Advance Too Short")
    model.add_item(FactoryBot.create(:item, is_borrowable: true, responsible: inventory_pool))
    # advance=1 → today invalid for Hauptlager; no alt pickup
    reservation = create_unsubmitted!(
      model: model,
      pickup_location_id: nil,
      start_date: Date.today,
      end_date: Date.today + 3.days
    )

    result = query(cart_query, user.id)
    expect(result[:errors]).to be_nil
    expect(result.dig(:data, :currentUser, :user, :unsubmittedOrder, :invalidReservationIds))
      .to include(reservation.id.to_s)
  end

  it "marks alt pickup when transfer buffer is violated but advance days are not" do
    model = FactoryBot.create(:leihs_model, transportable: true, product: "Buffer Too Short")
    model.add_item(FactoryBot.create(:item, is_borrowable: true, responsible: inventory_pool))
    # advance=1 → day+1 OK for Hauptlager; buffer=3 → day+1 invalid for alt
    reservation = create_unsubmitted!(
      model: model,
      pickup_location_id: location.id,
      start_date: 1.day.from_now,
      end_date: 4.days.from_now
    )

    result = query(cart_query, user.id)
    expect(result[:errors]).to be_nil
    expect(result.dig(:data, :currentUser, :user, :unsubmittedOrder, :invalidReservationIds))
      .to include(reservation.id.to_s)
  end
end
