require "spec_helper"
require_relative "../graphql_helper"

describe "models filtered by categoryId" do
  let(:user) { FactoryBot.create(:user) }
  let(:inventory_pool) { FactoryBot.create(:inventory_pool) }

  before do
    FactoryBot.create(:direct_access_right,
      inventory_pool: inventory_pool,
      user: user)
  end

  def borrowable_model
    FactoryBot.create(:leihs_model).tap do |model|
      FactoryBot.create(:item,
        leihs_model: model,
        responsible: inventory_pool,
        is_borrowable: true)
    end
  end

  def model_ids(category_id)
    result = query(<<-GRAPHQL, user.id, {categoryId: category_id})
      query($categoryId: UUID) {
        models(categoryId: $categoryId,
               orderBy: [{attribute: ID, direction: ASC}]) {
          edges { node { id } }
        }
      }
    GRAPHQL
    expect(result[:errors]).to be_nil
    result.dig(:data, :models, :edges).map { |e| e.dig(:node, :id) }
  end

  it "includes the category and its descendants, excludes ancestors and unrelated" do
    root_model = borrowable_model
    child_model = borrowable_model
    grandchild_model = borrowable_model
    unrelated_model = borrowable_model

    grandchild = FactoryBot.create(:category, direct_models: [grandchild_model])
    child = FactoryBot.create(:category,
      direct_models: [child_model],
      children: [grandchild])
    FactoryBot.create(:category, direct_models: [root_model], children: [child])
    FactoryBot.create(:category, direct_models: [unrelated_model])

    expect(model_ids(child.id))
      .to eq([child_model.id, grandchild_model.id].sort)
  end

  it "returns a model linked to the category and a descendant once" do
    model = borrowable_model
    child = FactoryBot.create(:category, direct_models: [model])
    parent = FactoryBot.create(:category, direct_models: [model], children: [child])

    expect(model_ids(parent.id)).to eq([model.id])
  end

  it "terminates on cyclic category links" do
    a_model = borrowable_model
    b_model = borrowable_model
    a = FactoryBot.create(:category, direct_models: [a_model])
    b = FactoryBot.create(:category, direct_models: [b_model], parents: [a])
    a.add_parent(b)

    expect(model_ids(a.id)).to eq([a_model.id, b_model.id].sort)
  end
end
