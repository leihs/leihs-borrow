require 'spec_helper'
require_relative '../graphql/graphql_helper'

describe 'refresh timeout' do
  let(:inventory_pool) do
    FactoryBot.create(:inventory_pool, id: '00843766-b48d-4a7d-89cc-565ced81bbf9')
  end

  let(:inventory_pool_2) do
    FactoryBot.create(:inventory_pool, id: 'e214f9e2-0813-42f0-8c23-6c9e3c8b15ba')
  end

  let(:inventory_pool_3) do
    FactoryBot.create(:inventory_pool, id: 'a6edf28c-59f3-478c-a0e3-d0b7831356c0')
  end

  let(:inventory_pool_no_access) do
    FactoryBot.create(:inventory_pool, id: '8d3f1896-ba08-4ff5-ab76-2993ca1d1edd')
  end

  let(:inventory_pool_holiday) do
    FactoryBot.create(:inventory_pool, id: 'bf040637-cf00-43b1-ab75-115eaebe81f8')
  end

  let(:inventory_pool_no_workday) do
    FactoryBot.create(:inventory_pool, id: '01d60223-cfe4-45d7-a1e0-73ad3b318e2d')
  end

  let(:user) do
    u = FactoryBot.create(:user, id: '8c360361-f70c-4b31-a271-b4050d4b9d26')
    [inventory_pool,
     inventory_pool_2,
     inventory_pool_3,
     inventory_pool_holiday,
     inventory_pool_no_workday].each do |ip|
      FactoryBot.create(:direct_access_right, inventory_pool: ip, user: u)
    end
    u
  end

  let(:user_2) do
    u = FactoryBot.create(:user, id: 'cd1eb17a-2cc0-4c08-a766-cdf4ba6bfe0f')
    FactoryBot.create(:direct_access_right,
                      inventory_pool: inventory_pool,
                      user: u)
    u
  end

  let(:model_1) do
    model = FactoryBot.create(:leihs_model,
                              id: 'db3197f4-7fef-4139-83e1-09f79abfa691')
    2.times do
      model.add_item(FactoryBot.create(:item,
                                       is_borrowable: true,
                                       responsible: inventory_pool))
    end
    model
  end

  let(:model_2) do
    model = FactoryBot.create(:leihs_model,
                              id: 'a9105e4f-4eef-4d44-8d2b-cb635a220c09')
    2.times do
      model.add_item(FactoryBot.create(:item,
                                       is_borrowable: true,
                                       responsible: inventory_pool))
    end
    model
  end

  let(:model_3) do
    model = FactoryBot.create(:leihs_model,
                              id: 'b0e1e686-2fed-4607-a3d9-9ae056282766')
    2.times do
      model.add_item(FactoryBot.create(:item,
                                       is_borrowable: true,
                                       responsible: inventory_pool))
    end
    model
  end

  let(:model_4) do
    model = FactoryBot.create(:leihs_model,
                              id: 'b58dc1cc-3114-4019-a173-49eed478bfdb')
    2.times do
      model.add_item(FactoryBot.create(:item,
                                       is_borrowable: true,
                                       responsible: inventory_pool))
    end
    model
  end

  let(:model_5) do
    model = FactoryBot.create(:leihs_model,
                              id: '510466c2-7c4d-49d5-819a-15e2513f2ae5')
    2.times do
      model.add_item(FactoryBot.create(:item,
                                       is_borrowable: true,
                                       responsible: inventory_pool_2))
    end
    model
  end

  let(:model_6) do
    model = FactoryBot.create(:leihs_model,
                              id: '755a44b2-7ae8-4325-98ea-bbc553f147bf')
    2.times do
      model.add_item(FactoryBot.create(:item,
                                       is_borrowable: true,
                                       responsible: inventory_pool_3))
    end
    model
  end

  let(:model_7) do
    model = FactoryBot.create(:leihs_model,
                              id: '5ba5c20a-4edc-4003-8240-a2f12d93968c')
    2.times do
      model.add_item(FactoryBot.create(:item,
                                       is_borrowable: true,
                                       responsible: inventory_pool_no_access))
    end
    model
  end

  let(:model_8) do
    model = FactoryBot.create(:leihs_model,
                              id: 'cb125cfe-026c-45ee-899e-ec768f7573f7')
    2.times do
      model.add_item(FactoryBot.create(:item,
                                       is_borrowable: true,
                                       responsible: inventory_pool_holiday))
    end
    model
  end

  let(:model_9) do
    model = FactoryBot.create(:leihs_model,
                              id: 'bcb3c469-63d7-48ad-8ea5-d0e3e7927cfd')
    2.times do
      model.add_item(FactoryBot.create(:item,
                                       is_borrowable: true,
                                       responsible: inventory_pool_no_workday))
    end
    model
  end

  let(:model_10) do
    model = FactoryBot.create(:leihs_model,
                              id: '0ec34a40-f528-411a-ad9c-7a6a7642df6c')
    2.times do
      model.add_item(FactoryBot.create(:item,
                                       is_borrowable: true,
                                       responsible: inventory_pool))
    end
    model
  end

  let(:model_without_items) do
    FactoryBot.create(:leihs_model,
                      id: '70e6153f-6a33-4942-a12d-dbd80ab4c156')
  end

  let(:m) do 
    <<-GRAPHQL
      mutation {
        refreshTimeout {
          unsubmittedOrder {
            validUntil
            reservations {
              id
              status
            }
            invalidReservationIds
          }
        }
      }
    GRAPHQL
  end

  def timed_out_date_time
    DateTime.now.utc - 31.minutes
  end

  it 'works' do
    r1a_invalid_start_date = FactoryBot.create(:reservation,
                                              id: '0a3ea476-f1e9-42e8-943f-35f7eefdad90',
                                              leihs_model: model_1,
                                              inventory_pool: inventory_pool,
                                              start_date: Date.yesterday,
                                              end_date: Date.tomorrow,
                                              user: user)
    #####################################################################################################
    # reservation_advance_days = 1
    Workday.find(inventory_pool_id: inventory_pool_2.id).update(reservation_advance_days: 1)
    r1b_invalid_reservation_advance_days = FactoryBot.create(:reservation,
                                                             id: 'de021090-cd50-4b1f-9448-5f7ba4367f1f',
                                                             leihs_model: model_5,
                                                             inventory_pool: inventory_pool_2,
                                                             start_date: Date.today,
                                                             end_date: Date.tomorrow,
                                                             user: user)
    #####################################################################################################
    # max visits count reached
    Workday.find(inventory_pool_id: inventory_pool_3.id).update(max_visits: {"1": "0",
                                                                             "2": "0",
                                                                             "3": "0",
                                                                             "4": "0",
                                                                             "5": "0",
                                                                             "6": "0",
                                                                             "0": "0"})
    r1c_max_visits_count_reached = FactoryBot.create(:reservation,
                                                     id: '7f2c1eb2-5f0a-4156-a21a-d3c116780458',
                                                     leihs_model: model_6,
                                                     inventory_pool: inventory_pool_3,
                                                     start_date: Date.today,
                                                     end_date: Date.tomorrow,
                                                     user: user)
    #####################################################################################################
    # no access for inventory pool
    r1d_no_access_to_pool = FactoryBot.create(:reservation,
                                              id: 'c7557286-b375-48a4-8b90-52e35ba65a07',
                                              leihs_model: model_7,
                                              inventory_pool: inventory_pool_no_access,
                                              start_date: Date.today,
                                              end_date: Date.tomorrow,
                                              user: user)
    #####################################################################################################
    # holidays
    FactoryBot.create(:holiday,
                      start_date: Date.tomorrow,
                      end_date: Date.tomorrow,
                      inventory_pool: inventory_pool_holiday)
    r1e_holiday = FactoryBot.create(:reservation,
                                    id: 'd08550f3-3717-43e5-91a1-4c36cbfa44f8',
                                    leihs_model: model_8,
                                    inventory_pool: inventory_pool_holiday,
                                    start_date: Date.today,
                                    end_date: Date.tomorrow,
                                    user: user)
    #####################################################################################################
    # not a workday
    Workday.find(inventory_pool_id: inventory_pool_no_workday.id)
      .update(monday: false, 
              tuesday: false, 
              wednesday: false, 
              thursday: false, 
              friday: false, 
              saturday: false, 
              sunday: false)
    r1f_no_workday = FactoryBot.create(:reservation,
                                       id: '09bf1e92-82e0-4b6c-b944-e483933a0ea2',
                                       leihs_model: model_9,
                                       inventory_pool: inventory_pool_no_workday,
                                       start_date: Date.today,
                                       end_date: Date.tomorrow,
                                       user: user)
    #####################################################################################################
    # maximum reservation time
    Settings.first.update(maximum_reservation_time: 7)
    r1g_max_reservation_time = FactoryBot.create(:reservation,
                                                 id: '206bfb67-5997-4318-965a-689db5990c70',
                                                 leihs_model: model_10,
                                                 inventory_pool: inventory_pool,
                                                 start_date: Date.today,
                                                 end_date: (Date.today + 8.days),
                                                 user: user)
    #####################################################################################################
    # 1 item was retired after reservation was created
    r2_not_timed_out_with_invalid_avail_1 = FactoryBot.create(:reservation,
                                                              id: 'd5303020-d9c7-4e71-b882-5963fc516726',
                                                              leihs_model: model_without_items,
                                                              inventory_pool: inventory_pool,
                                                              start_date: Date.today,
                                                              end_date: Date.tomorrow,
                                                              user: user)
    #####################################################################################################
    r3_timed_out_with_invalid_avail_1 = FactoryBot.create(:reservation,
                                                          id: 'e894f7df-7ef2-4b84-a9a4-1d56dfa202c9',
                                                          leihs_model: model_2,
                                                          inventory_pool: inventory_pool,
                                                          start_date: Date.today,
                                                          end_date: Date.tomorrow,
                                                          user: user,
                                                          created_at: timed_out_date_time,
                                                          updated_at: timed_out_date_time)
    r3_timed_out_with_invalid_avail_2 = FactoryBot.create(:reservation,
                                                          id: 'dbb412c5-c3b2-40b6-9adf-c3d3377b600e',
                                                          leihs_model: model_2,
                                                          inventory_pool: inventory_pool,
                                                          start_date: Date.today,
                                                          end_date: Date.tomorrow,
                                                          user: user,
                                                          created_at: timed_out_date_time,
                                                          updated_at: timed_out_date_time)
    # quantity of 1 taken away by another user after timeout
    FactoryBot.create(:reservation,
                      id: '6b044a17-b088-43e3-b104-e721cab5af36',
                      leihs_model: model_2,
                      inventory_pool: inventory_pool,
                      start_date: Date.today,
                      end_date: Date.tomorrow,
                      user: user_2)
    #####################################################################################################
    r4_timed_out_ok = FactoryBot.create(:reservation,
                                        id: 'd57b51b7-e83e-4687-ac1f-3d6d4e1a3f45',
                                        leihs_model: model_3,
                                        inventory_pool: inventory_pool,
                                        start_date: Date.today,
                                        end_date: Date.tomorrow,
                                        user: user,
                                        created_at: timed_out_date_time,
                                        updated_at: timed_out_date_time)
    #####################################################################################################
    r5_not_timed_out_ok = FactoryBot.create(:reservation,
                                            id: '89ca0106-34e4-4131-b64c-6bcc703b4489',
                                            leihs_model: model_4,
                                            inventory_pool: inventory_pool,
                                            start_date: Date.today,
                                            end_date: Date.tomorrow,
                                            user: user)
    #####################################################################################################

    m_result = query(m, user.id).deep_symbolize_keys
    expect(m_result[:errors]).to be_nil

    reservations = m_result.dig(:data, :refreshTimeout, :unsubmittedOrder, :reservations)
    valid_until = if vu = m_result.dig(:data, :refreshTimeout, :unsubmittedOrder, :validUntil)
                    DateTime.parse(vu)
                  end
    unsubmitted_ids = reservations.select { |r| r[:status] == 'UNSUBMITTED' }.map { |r| r[:id] }
    draft_ids = reservations.select { |r| r[:status] == 'DRAFT' }.map { |r| r[:id] }
    invalid_ids = m_result.dig(:data, :refreshTimeout, :unsubmittedOrder, :invalidReservationIds)

    expect(unsubmitted_ids.to_set).to eq [r4_timed_out_ok, r5_not_timed_out_ok].map(&:id).to_set
    expect(draft_ids.to_set).to eq [r1a_invalid_start_date,
                                    r1b_invalid_reservation_advance_days,
                                    r1c_max_visits_count_reached,
                                    r1d_no_access_to_pool,
                                    r1e_holiday,
                                    r1f_no_workday,
                                    r1g_max_reservation_time,
                                    r2_not_timed_out_with_invalid_avail_1,
                                    r3_timed_out_with_invalid_avail_1,
                                    r3_timed_out_with_invalid_avail_2].map(&:id).to_set
    expect(invalid_ids.to_set).to eq [r1a_invalid_start_date,
                                      r1b_invalid_reservation_advance_days,
                                      r1c_max_visits_count_reached,
                                      r1d_no_access_to_pool,
                                      r1e_holiday,
                                      r1f_no_workday,
                                      r1g_max_reservation_time,
                                      r2_not_timed_out_with_invalid_avail_1,
                                      r3_timed_out_with_invalid_avail_1,
                                      r3_timed_out_with_invalid_avail_2].map(&:id).to_set
    expect(valid_until.try(:utc)).to eq (DateTime.now.utc + 30.minutes).change(usec: 0)
  end
end
