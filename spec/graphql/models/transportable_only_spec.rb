require "spec_helper"
require_relative "../graphql_helper"

describe "models transportableOnly filter" do
  let(:user) { FactoryBot.create(:user) }
  let(:inventory_pool) { FactoryBot.create(:inventory_pool) }
  let!(:access_right) do
    FactoryBot.create(:direct_access_right,
      inventory_pool: inventory_pool,
      user: user)
  end
  let!(:transportable_model) do
    model = FactoryBot.create(:leihs_model, transportable: true, product: "Transportable Cam")
    FactoryBot.create(:item,
      leihs_model: model,
      responsible: inventory_pool,
      is_borrowable: true)
    model
  end
  let!(:non_transportable_model) do
    model = FactoryBot.create(:leihs_model, transportable: false, product: "Fixed Desk")
    FactoryBot.create(:item,
      leihs_model: model,
      responsible: inventory_pool,
      is_borrowable: true)
    model
  end
  let!(:pickup_location) do
    FactoryBot.create(:pickup_location,
      inventory_pool: inventory_pool,
      name: "Pickup Location #1")
  end

  def model_ids(result)
    result.dig(:data, :models, :edges).map { |edge| edge.dig(:node, :id) }
  end

  it "returns all reservable models for the pool without transportableOnly" do
    q = <<-GRAPHQL
      {
        models(
          poolIds: ["#{inventory_pool.id}"],
          orderBy: [{attribute: NAME, direction: ASC}]
        ) {
          edges {
            node {
              id
              transportable
            }
          }
        }
      }
    GRAPHQL

    result = query(q, user.id).deep_symbolize_keys
    expect(result[:errors]).to be_nil
    expect(model_ids(result)).to contain_exactly(
      transportable_model.id,
      non_transportable_model.id
    )
  end

  it "returns only transportable models when transportableOnly is true" do
    q = <<-GRAPHQL
      {
        models(
          poolIds: ["#{inventory_pool.id}"],
          transportableOnly: true,
          orderBy: [{attribute: NAME, direction: ASC}]
        ) {
          edges {
            node {
              id
              transportable
            }
          }
        }
      }
    GRAPHQL

    result = query(q, user.id).deep_symbolize_keys
    expect(result[:errors]).to be_nil
    expect(model_ids(result)).to eq [transportable_model.id]
    expect(result.dig(:data, :models, :edges).first.dig(:node, :transportable)).to eq true
  end
end
