require 'spec_helper'
require_relative 'graphql_helper'

describe 'rentals' do
  before :example do
    Settings.first.update(deliver_received_order_notifications: true)
    SystemAndSecuritySettings.first.update(external_base_url: LEIHS_BORROW_HTTP_BASE_URL)
  end

  let(:user) do
    FactoryBot.create(
      :user,
      id: '67f552ac-ff81-494d-9de1-2c8f2bdcb3fd'
    )
  end

  let(:inventory_pool_1) do
    FactoryBot.create(
      :inventory_pool,
      id: 'ef2eec3e-54ce-44ec-887f-5032f3df63f3'
    )
  end

  let(:inventory_pool_2) do
    FactoryBot.create(
      :inventory_pool,
      id: 'c4106f5f-e063-4793-a60f-e94f87cac89a',
      is_active: false
    )
  end

  let(:model_1) do
    FactoryBot.create(:leihs_model,
                      id: '9ac1cc1d-d7ef-4098-891b-9d6b8c53d0f7')
  end

  let(:model_2) do
    FactoryBot.create(:leihs_model,
                      id: '85875d44-4052-4c8e-a722-70bac0ab78bb')
  end

  let(:model_3) do
    FactoryBot.create(:leihs_model,
                      id: '0710ed27-d244-4779-8e3b-e30a2866f51b')
  end

  before(:example) do
    FactoryBot.create(:direct_access_right,
                      inventory_pool: inventory_pool_1,
                      user: user)

    FactoryBot.create(:direct_access_right,
                      inventory_pool: inventory_pool_2,
                      user: user)
  end

  def create_orderless_contract
    model_1.add_item(
      FactoryBot.create(:item,
                        is_borrowable: true,
                        responsible: inventory_pool_1)
    )

    c1_id = '42c2ec6e-b62c-4c88-be1e-75565278b127'
    Contract.create_with_disabled_triggers(c1_id,
                                           user.id,
                                           inventory_pool_1.id)

    r1 = FactoryBot.create(:reservation,
                           leihs_model: model_1,
                           inventory_pool: inventory_pool_1,
                           status: 'signed',
                           contract_id: c1_id,
                           user: user)
    Contract.find(id: c1_id)
  end

  def create_customer_order_with_contract
    i = FactoryBot.create(:item,
                          leihs_model: model_1,
                          is_borrowable: true,
                          responsible: inventory_pool_1)

    c1_id = 'e1828e88-dec8-4430-963f-2a528aeb7d60'
    Contract.create_with_disabled_triggers(c1_id,
                                           user.id,
                                           inventory_pool_1.id)

    o = FactoryBot.create(:order,
                          id: 'cd57c6e1-0c71-417a-9923-5cdf20dede82',
                          user: user)

    po = FactoryBot.create(:pool_order,
                           user: user,
                           inventory_pool: inventory_pool_1,
                           order: o,
                           state: 'approved')

    r1 = FactoryBot.create(:reservation,
                           leihs_model: model_1,
                           inventory_pool: inventory_pool_1,
                           status: 'signed',
                           contract_id: c1_id,
                           order_id: po.id, 
                           user: user)
    o
  end

  def create_customer_order_without_contract
    i = FactoryBot.create(:item,
                          leihs_model: model_1,
                          is_borrowable: true,
                          responsible: inventory_pool_1)

    o = FactoryBot.create(:order,
                          id: 'c18014e3-917c-4ae3-8277-b3130739302a',
                          user: user)

    po = FactoryBot.create(:pool_order,
                           user: user,
                           inventory_pool: inventory_pool_1,
                           order: o,
                           state: 'submitted')

    r1 = FactoryBot.create(:reservation,
                           leihs_model: model_1,
                           inventory_pool: inventory_pool_1,
                           status: 'submitted',
                           order_id: po.id, 
                           user: user)
    o
  end

  def create_hand_over_reservation
    i = FactoryBot.create(:item,
                          leihs_model: model_1,
                          is_borrowable: true,
                          responsible: inventory_pool_1)
    r1 = FactoryBot.create(:reservation,
                           leihs_model: model_1,
                           item_id: i.id, 
                           inventory_pool: inventory_pool_1,
                           status: 'approved',
                           user: user)
    r1
  end

  def create_draft_reservation
    r1 = FactoryBot.create(:reservation,
                           inventory_pool: inventory_pool_1,
                           status: 'draft',
                           user: user)
    r1
  end

  def create_unsubmitted_reservation
    r1 = FactoryBot.create(:reservation,
                           inventory_pool: inventory_pool_1,
                           status: 'unsubmitted',
                           user: user)
    r1
  end

  def uuid(*args)
    t = ['customer_order', *args].join('_')
    database["SELECT uuid_generate_v5(uuid_ns_dns(), '#{t}') AS r"].first[:r]
  end

  it 'index' do
    create_draft_reservation
    create_unsubmitted_reservation

    coc = create_orderless_contract
    ccowc = create_customer_order_with_contract
    ccowoc = create_customer_order_without_contract
    chor = create_hand_over_reservation

    q = <<-GRAPHQL
      query {
        rentals {
          edges {
            node {
              id
            }
          }
        }
      }
    GRAPHQL


    result = query(q, user.id).deep_symbolize_keys
    r_ids = result.dig(:data, :rentals, :edges).map{|e| e[:node]}.map{|e| e[:id]}
    expect(r_ids.count).to eq 4
    expect(r_ids).to include uuid(coc.id)
    expect(r_ids).to include uuid(chor.user_id, chor.inventory_pool_id)
    expect(r_ids).to include ccowc.id
    expect(r_ids).to include ccowoc.id
  end
end
