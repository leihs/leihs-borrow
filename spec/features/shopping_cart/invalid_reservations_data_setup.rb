RSpec.configure do |rspec|
  rspec.shared_context_metadata_behavior = :apply_to_host_groups
end

# This will arrange a cart with reservations, each of them violating a constraint.
# Typical case: item was taken by another user after the reservation timeout duration.
# Other cases are not directly related to the reservation timeout, but are also
# a result of something having happened between when the item was added to the cart and now
# (e.g. item does not exist in pool anymore).

# NOTE: UUIDs are hardcoded to quickly find the context from a test failure message cointaining an ID

RSpec.shared_context "invalid reservations data setup", :shared_context => :metadata do
  let(:now) { DateTime.now.utc }

  # Structure data (pools, models etc)
  begin
    let(:inventory_pool) do
      FactoryBot.create(:inventory_pool, id: "00843766-b48d-4a7d-89cc-565ced81bbf9", name: "Pool One")
    end

    let(:inventory_pool_2) do
      FactoryBot.create(:inventory_pool, id: "e214f9e2-0813-42f0-8c23-6c9e3c8b15ba", name: "Pool Two")
    end

    let(:inventory_pool_3_max_visits) do
      FactoryBot.create(:inventory_pool, id: "a6edf28c-59f3-478c-a0e3-d0b7831356c0", name: "Pool Three")
    end

    let(:inventory_pool_4_advance_days) do
      FactoryBot.create(:inventory_pool, id: "37f689af-458b-4173-a3c5-cb6ca7f29a2f", name: "Pool Four")
    end

    let(:inventory_pool_5_no_access) do
      FactoryBot.create(:inventory_pool, id: "8d3f1896-ba08-4ff5-ab76-2993ca1d1edd", name: "Pool Five")
    end

    let(:inventory_pool_6_holiday) do
      FactoryBot.create(:inventory_pool, id: "bf040637-cf00-43b1-ab75-115eaebe81f8", name: "Pool Six")
    end

    let(:inventory_pool_7_no_workday) do
      FactoryBot.create(:inventory_pool, id: "01d60223-cfe4-45d7-a1e0-73ad3b318e2d", name: "Pool Seven")
    end

    let(:inventory_pool_8_suspended) do
      FactoryBot.create(:inventory_pool, id: "ee7fe76b-6182-45d0-aa4a-e8bf6a76bcbf", name: "Pool Eight with Suspension")
    end

    let(:user) do
      u = FactoryBot.create(:user, id: "8c360361-f70c-4b31-a271-b4050d4b9d26")
      [inventory_pool,
       inventory_pool_2,
       inventory_pool_3_max_visits,
       inventory_pool_4_advance_days,
       inventory_pool_6_holiday,
       inventory_pool_7_no_workday,
       inventory_pool_8_suspended].each do |ip|
        FactoryBot.create(:direct_access_right, inventory_pool: ip, user: u)
      end

      # user is currently suspended in a certain pool
      FactoryBot.create(:suspension, user: u, inventory_pool: inventory_pool_8_suspended)

      u
    end

    let(:user_2) do
      u = FactoryBot.create(:user, id: "cd1eb17a-2cc0-4c08-a766-cdf4ba6bfe0f")
      FactoryBot.create(:direct_access_right,
                        inventory_pool: inventory_pool,
                        user: u)
      u
    end

    # this user is "blocking" the maximum daily visits of a pool with returns and pickup
    let(:user_3_max_visits) do
      u = FactoryBot.create(:user, id: "ebe016ce-3d73-4e5a-ac90-3475b43b8def")
      FactoryBot.create(:direct_access_right,
                        inventory_pool: inventory_pool_3_max_visits,
                        user: u)
      u
    end

    let(:model_1) do
      model = FactoryBot.create(:leihs_model, product: "Start Date In Past",
                                              id: "db3197f4-7fef-4139-83e1-09f79abfa691")
      2.times do
        model.add_item(FactoryBot.create(:item,
                                         is_borrowable: true,
                                         responsible: inventory_pool))
      end
      model
    end

    let(:model_2) do
      model = FactoryBot.create(:leihs_model, product: "Quantity Too High",
                                              id: "a9105e4f-4eef-4d44-8d2b-cb635a220c09")
      2.times do
        model.add_item(FactoryBot.create(:item, is_borrowable: true,
                                                responsible: inventory_pool))
      end
      model
    end

    let(:model_3) do
      model = FactoryBot.create(:leihs_model, product: "OK and Timed Out",
                                              id: "b0e1e686-2fed-4607-a3d9-9ae056282766")
      2.times do
        model.add_item(FactoryBot.create(:item,
                                         is_borrowable: true,
                                         responsible: inventory_pool))
      end
      model
    end

    let(:model_4) do
      model = FactoryBot.create(:leihs_model, product: "OK and Not Timed Out",
                                              id: "b58dc1cc-3114-4019-a173-49eed478bfdb")
      2.times do
        model.add_item(FactoryBot.create(:item,
                                         is_borrowable: true,
                                         responsible: inventory_pool))
      end
      model
    end

    let(:model_5) do
      model = FactoryBot.create(:leihs_model, product: "Reservation Advance Days",
                                              id: "510466c2-7c4d-49d5-819a-15e2513f2ae5")
      2.times do
        model.add_item(FactoryBot.create(:item,
                                         is_borrowable: true,
                                         responsible: inventory_pool_4_advance_days))
      end
      model
    end

    let(:model_6) do
      model = FactoryBot.create(:leihs_model, product: "Max Visits Count Pickup",
                                              id: "755a44b2-7ae8-4325-98ea-bbc553f147bf")
      2.times do
        model.add_item(FactoryBot.create(:item,
                                         is_borrowable: true,
                                         responsible: inventory_pool_3_max_visits))
      end
      model
    end

    let(:model_6_return) do
      model = FactoryBot.create(:leihs_model, product: "Max Visits Count Return",
                                              id: "eb20fabe-768d-4285-8c65-47f0c11f5d7d")
      2.times do
        model.add_item(FactoryBot.create(:item,
                                         is_borrowable: true,
                                         responsible: inventory_pool_3_max_visits))
      end
      model
    end

    let(:model_6_x) do
      model = FactoryBot.create(:leihs_model, product: "Another User is Returning This Causing Max Visits Reached",
                                              id: "83ecc0e0-2bfe-4548-86cb-1ed43bf31014")
      2.times do
        model.add_item(FactoryBot.create(:item,
                                         is_borrowable: true,
                                         responsible: inventory_pool_3_max_visits))
      end
      model
    end

    let(:model_7) do
      model = FactoryBot.create(:leihs_model, product: "No Access To Pool",
                                              id: "5ba5c20a-4edc-4003-8240-a2f12d93968c")
      2.times do
        model.add_item(FactoryBot.create(:item,
                                         is_borrowable: true,
                                         responsible: inventory_pool_5_no_access))
      end
      model
    end

    let(:model_8) do
      model = FactoryBot.create(:leihs_model, product: "Holiday on End Date",
                                              id: "cb125cfe-026c-45ee-899e-ec768f7573f7")
      2.times do
        model.add_item(FactoryBot.create(:item,
                                         is_borrowable: true,
                                         responsible: inventory_pool_6_holiday))
      end
      model
    end

    let(:model_9) do
      model = FactoryBot.create(:leihs_model, product: "Not A Workday",
                                              id: "bcb3c469-63d7-48ad-8ea5-d0e3e7927cfd")
      2.times do
        model.add_item(FactoryBot.create(:item,
                                         is_borrowable: true,
                                         responsible: inventory_pool_7_no_workday))
      end
      model
    end

    let(:model_10) do
      model = FactoryBot.create(:leihs_model, product: "Maximum Reservation Time",
                                              id: "0ec34a40-f528-411a-ad9c-7a6a7642df6c")
      2.times do
        model.add_item(FactoryBot.create(:item,
                                         is_borrowable: true,
                                         responsible: inventory_pool))
      end
      model
    end

    let(:model_11_suspended) do
      model = FactoryBot.create(:leihs_model, product: "User is Suspended",
                                              id: "816bdd15-fc4f-4c8f-a464-a33fb2009f56")
      2.times do
        model.add_item(FactoryBot.create(:item,
                                         is_borrowable: true,
                                         responsible: inventory_pool_8_suspended))
      end
      model
    end

    let(:model_without_items) do
      FactoryBot.create(:leihs_model, product: "Model With No Items",
                                      id: "70e6153f-6a33-4942-a12d-dbd80ab4c156")
    end
  end

  ##################################################################################

  def timed_out_date_time
    now.utc - 31.minutes
  end

  #####################################################################################################
  let(:r1a_start_date_in_past) do
    FactoryBot.create(:reservation,
                      id: "0a3ea476-f1e9-42e8-943f-35f7eefdad90",
                      leihs_model: model_1,
                      inventory_pool: inventory_pool,
                      start_date: Date.yesterday,
                      end_date: Date.tomorrow,
                      user: user)
  end

  #####################################################################################################
  let(:r1b_invalid_reservation_advance_days) do
    test_advance_days = 3
    Workday.find(inventory_pool_id: inventory_pool_4_advance_days.id).update(reservation_advance_days: test_advance_days)
    FactoryBot.create(:reservation,
                      id: "de021090-cd50-4b1f-9448-5f7ba4367f1f",
                      leihs_model: model_5,
                      inventory_pool: inventory_pool_4_advance_days,
                      start_date: (test_advance_days - 1).days.from_now,
                      end_date: (test_advance_days + 1).days.from_now,
                      user: user)
  end

  #####################################################################################################

  # max visits count reached: only 1 is allowed, but
  #   * another user already has a pickup on the day I would pickup
  #   * another user already has a pickup on the day I would return
  #   * TODO: another user already has a signed order with return on the day I would visit
  let(:r1c_max_visits_count_reached) do
    Workday.find(inventory_pool_id: inventory_pool_3_max_visits.id).update(max_visits: { "1": "1",
                                                                                         "2": "1",
                                                                                         "3": "1",
                                                                                         "4": "1",
                                                                                         "5": "1",
                                                                                         "6": "1",
                                                                                         "0": "1" })

    user_3_max_visits_customer_order = FactoryBot.create(:order,
                                                         user: user_3_max_visits)
    user_3_max_visits_order = FactoryBot.create(:pool_order,
                                                user: user_3_max_visits,
                                                inventory_pool: inventory_pool_3_max_visits,
                                                state: :submitted,
                                                order: user_3_max_visits_customer_order)

    # this reservations pickup-visit blocks my pickup (in 4 days)
    FactoryBot.create(:reservation,
                      id: "3d98b789-5dd5-48ed-9002-79623be71789",
                      status: :submitted,
                      order: user_3_max_visits_order,
                      leihs_model: model_6_x,
                      inventory_pool: inventory_pool_3_max_visits,
                      start_date: 4.days.from_now,
                      end_date: 6.days.from_now,
                      user: user_3_max_visits)
    # this reservations pickup-visit blocks my return (in 12 days)
    FactoryBot.create(:reservation,
                      id: "dcef87fc-2e09-4a30-8492-22acd1c27215",
                      status: :submitted,
                      order: user_3_max_visits_order,
                      leihs_model: model_6_x,
                      inventory_pool: inventory_pool_3_max_visits,
                      start_date: 12.days.from_now,
                      end_date: 14.days.from_now,
                      user: user_3_max_visits)

    # this reservation is blocked at the pickup (in 4 days)
    max_visits_count_reached_at_pickup =
      FactoryBot.create(:reservation,
                        id: "7f2c1eb2-5f0a-4156-a21a-d3c116780458",
                        leihs_model: model_6,
                        inventory_pool: inventory_pool_3_max_visits,
                        start_date: 4.days.from_now,
                        end_date: 6.days.from_now,
                        user: user)
    # this reservation is blocked at the return (in 12 days)
    max_visits_count_reached_at_return =
      FactoryBot.create(:reservation,
                        id: "314446a7-7d07-48d6-b7a3-5fbf66e693d1",
                        leihs_model: model_6_return,
                        inventory_pool: inventory_pool_3_max_visits,
                        start_date: 10.days.from_now,
                        end_date: 12.days.from_now,
                        user: user)
    return [max_visits_count_reached_at_pickup, max_visits_count_reached_at_return]
  end

  #####################################################################################################
  # no access for inventory pool
  let(:r1d_no_access_to_pool) do
    FactoryBot.create(:reservation,
                      id: "c7557286-b375-48a4-8b90-52e35ba65a07",
                      leihs_model: model_7,
                      inventory_pool: inventory_pool_5_no_access,
                      start_date: Date.today,
                      end_date: Date.tomorrow,
                      user: user)
  end

  #####################################################################################################
  # holidays
  let(:r1e_holiday) do
    FactoryBot.create(:holiday,
                      start_date: 8.days.from_now,
                      end_date: 8.days.from_now,
                      inventory_pool: inventory_pool_6_holiday)
    FactoryBot.create(:reservation,
                      id: "d08550f3-3717-43e5-91a1-4c36cbfa44f8",
                      leihs_model: model_8,
                      inventory_pool: inventory_pool_6_holiday,
                      start_date: 7.days.from_now,
                      end_date: 8.days.from_now,
                      user: user)
  end

  #####################################################################################################
  # not a workday
  # NOTE: maybe there should be an "open" day, or the reservation is "never" resolvable if they never work?
  let(:r1f_no_workday) do
    Workday.find(inventory_pool_id: inventory_pool_7_no_workday.id)
      .update(monday: false,
              tuesday: false,
              wednesday: false,
              thursday: false,
              friday: false,
              saturday: false,
              sunday: false)
    FactoryBot.create(:reservation,
                      id: "09bf1e92-82e0-4b6c-b944-e483933a0ea2",
                      leihs_model: model_9,
                      inventory_pool: inventory_pool_7_no_workday,
                      start_date: Date.today,
                      end_date: Date.tomorrow,
                      user: user)
  end

  #####################################################################################################
  # maximum reservation time
  let(:r1g_max_reservation_time) do
    Settings.first.update(maximum_reservation_time: 7)
    FactoryBot.create(:reservation,
                      id: "206bfb67-5997-4318-965a-689db5990c70",
                      leihs_model: model_10,
                      inventory_pool: inventory_pool,
                      start_date: Date.today,
                      end_date: (Date.today + 8.days),
                      user: user)
  end

  #####################################################################################################
  # 1 item was retired after reservation was created
  let(:r2_not_timed_out_with_invalid_avail_1) do
    FactoryBot.create(:reservation,
                      id: "d5303020-d9c7-4e71-b882-5963fc516726",
                      leihs_model: model_without_items,
                      inventory_pool: inventory_pool,
                      start_date: Date.today,
                      end_date: Date.tomorrow,
                      user: user)
  end

  #####################################################################################################
  let(:r3_timed_out_with_invalid_avail) do
    timed_out_with_invalid_avail_1 = FactoryBot.create(:reservation,
                                                       id: "e894f7df-7ef2-4b84-a9a4-1d56dfa202c9",
                                                       leihs_model: model_2,
                                                       inventory_pool: inventory_pool,
                                                       start_date: Date.today,
                                                       end_date: Date.tomorrow,
                                                       user: user,
                                                       created_at: timed_out_date_time,
                                                       updated_at: timed_out_date_time)
    timed_out_with_invalid_avail_2 = FactoryBot.create(:reservation,
                                                       id: "dbb412c5-c3b2-40b6-9adf-c3d3377b600e",
                                                       leihs_model: model_2,
                                                       inventory_pool: inventory_pool,
                                                       start_date: Date.today,
                                                       end_date: Date.tomorrow,
                                                       user: user,
                                                       created_at: timed_out_date_time,
                                                       updated_at: timed_out_date_time)
    # quantity of 1 taken away by another user after timeout
    FactoryBot.create(:reservation,
                      id: "6b044a17-b088-43e3-b104-e721cab5af36",
                      leihs_model: model_2,
                      inventory_pool: inventory_pool,
                      start_date: Date.today,
                      end_date: Date.tomorrow,
                      user: user_2)

    return [timed_out_with_invalid_avail_1, timed_out_with_invalid_avail_2]
  end

  #####################################################################################################
  let(:r4_timed_out_ok) do
    FactoryBot.create(:reservation,
                      id: "d57b51b7-e83e-4687-ac1f-3d6d4e1a3f45",
                      leihs_model: model_3,
                      inventory_pool: inventory_pool,
                      start_date: Date.today,
                      end_date: Date.tomorrow,
                      user: user,
                      created_at: timed_out_date_time,
                      updated_at: timed_out_date_time)
  end

  #####################################################################################################
  let(:r5_not_timed_out_ok) do
    FactoryBot.create(:reservation,
                      id: "89ca0106-34e4-4131-b64c-6bcc703b4489",
                      leihs_model: model_4,
                      inventory_pool: inventory_pool,
                      start_date: Date.today,
                      end_date: Date.tomorrow,
                      user: user)
  end

  #####################################################################################################
  let(:r6_user_is_suspended) do
    FactoryBot.create(:reservation,
                      id: "b403139c-9b46-48ba-8aca-d35e2f1e05ab",
                      leihs_model: model_11_suspended,
                      inventory_pool: inventory_pool_8_suspended,
                      start_date: Date.today,
                      end_date: Date.tomorrow,
                      user: user)
  end

  #####################################################################################################
  
  let(:r1c_max_visits_count_reached_at_pickup) do
    r1c_max_visits_count_reached.first
  end

  let(:r1c_max_visits_count_reached_at_return) do
    r1c_max_visits_count_reached.second
  end

  let(:r3_timed_out_with_invalid_avail_1) do
    r3_timed_out_with_invalid_avail.first
  end

  let(:r3_timed_out_with_invalid_avail_2) do
    r3_timed_out_with_invalid_avail.second
  end

  # Example specs can call this to force all reservations to be created in the database (without explicitly calling each of them)
  let(:create_all_sample_reservations) do
    {
      :r1a_start_date_in_past => r1a_start_date_in_past,
      :r1b_invalid_reservation_advance_days => r1b_invalid_reservation_advance_days,
      :r1c_max_visits_count_reached_at_pickup => r1c_max_visits_count_reached_at_pickup,
      :r1c_max_visits_count_reached_at_return => r1c_max_visits_count_reached_at_return,
      :r1d_no_access_to_pool => r1d_no_access_to_pool,
      :r1e_holiday => r1e_holiday,
      :r1f_no_workday => r1f_no_workday,
      :r1g_max_reservation_time => r1g_max_reservation_time,
      :r2_not_timed_out_with_invalid_avail_1 => r2_not_timed_out_with_invalid_avail_1,
      :r3_timed_out_with_invalid_avail_1 => r3_timed_out_with_invalid_avail_1,
      :r3_timed_out_with_invalid_avail_2 => r3_timed_out_with_invalid_avail_2,
      :r4_timed_out_ok => r4_timed_out_ok,
      :r5_not_timed_out_ok => r5_not_timed_out_ok,
      :r6_user_is_suspended => r6_user_is_suspended
    }
  end
end

RSpec.configure do |rspec|
  rspec.include_context "invalid reservations data setup", :include_shared => true
end
