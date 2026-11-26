require "spec_helper"
require_relative "../graphql_helper"

describe "inventoryPool.pickupLocations" do
  it "returns pickup locations ordered by name, and empty list when none" do
    pool_with_locations = FactoryBot.create(
      :inventory_pool,
      name: "Pool With Locations",
      default_pickup_location_name: "Main warehouse",
      enable_alternative_pickup_locations: true
    )
    pool_without_locations = FactoryBot.create(
      :inventory_pool,
      name: "Pool Without Locations"
    )

    loc_b = FactoryBot.create(
      :pickup_location,
      inventory_pool: pool_with_locations,
      name: "Beta Site",
      description: "Second alternative"
    )
    loc_a = FactoryBot.create(
      :pickup_location,
      inventory_pool: pool_with_locations,
      name: "Alpha Site",
      description: "First alternative"
    )
    FactoryBot.create(
      :pickup_location,
      inventory_pool: pool_with_locations,
      name: "Inactive Site",
      description: "Not shown",
      active: false
    )

    user = FactoryBot.create(
      :user,
      access_rights: [
        FactoryBot.create(:direct_access_right,
          role: :customer,
          inventory_pool: pool_with_locations),
        FactoryBot.create(:direct_access_right,
          role: :customer,
          inventory_pool: pool_without_locations)
      ]
    )

    q = <<-GRAPHQL
      {
        currentUser {
          user {
            inventoryPools(orderBy: [{attribute: NAME, direction: ASC}]) {
              name
              defaultPickupLocationName
              pickupLocations {
                id
                name
                description
              }
            }
          }
        }
      }
    GRAPHQL

    result = query(q, user.id)

    expect_graphql_result(result, {
      currentUser: {
        user: {
          inventoryPools: [
            {
              name: "Pool With Locations",
              defaultPickupLocationName: "Main warehouse",
              pickupLocations: [
                {id: loc_a.id.to_s, name: "Alpha Site", description: "First alternative"},
                {id: loc_b.id.to_s, name: "Beta Site", description: "Second alternative"}
              ]
            },
            {
              name: "Pool Without Locations",
              defaultPickupLocationName: "Hauptlager",
              pickupLocations: []
            }
          ]
        }
      }
    })
  end

  it "hides pickup locations when the feature is disabled for the pool" do
    pool = FactoryBot.create(
      :inventory_pool,
      name: "Pool With Disabled Feature",
      enable_alternative_pickup_locations: false
    )
    FactoryBot.create(:pickup_location, inventory_pool: pool, name: "Alpha Site")

    user = FactoryBot.create(
      :user,
      access_rights: [
        FactoryBot.create(:direct_access_right, role: :customer, inventory_pool: pool)
      ]
    )

    q = <<-GRAPHQL
      {
        currentUser {
          user {
            inventoryPools(orderBy: [{attribute: NAME, direction: ASC}]) {
              name
              pickupLocations {
                id
                name
              }
            }
          }
        }
      }
    GRAPHQL

    result = query(q, user.id)

    expect_graphql_result(result, {
      currentUser: {
        user: {
          inventoryPools: [
            {name: "Pool With Disabled Feature", pickupLocations: []}
          ]
        }
      }
    })
  end
end
