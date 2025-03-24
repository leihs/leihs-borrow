RSpec.shared_context "reservation advance days" do
  before do
    days_of_week = [:sunday,
      :monday,
      :tuesday,
      :wednesday,
      :thursday,
      :friday,
      :saturday]

    # Update the workday for the next day to be closed.
    closed_date_1 = Date.today + 1.day
    closed_day_1 =
      days_of_week
        .cycle
        .with_index
        .detect { |_, idx| idx == closed_date_1.wday }
        .first

    # Update the workday for day after that to be closed but processing orders.
    closed_date_2 = closed_date_1 + 1.day
    closed_day_2 =
      days_of_week
        .cycle
        .with_index
        .detect { |_, idx| idx == closed_date_2.wday }
        .first

    @inventory_pool.workday.update(closed_day_1 => false,
      "#{closed_day_1}_orders_processing" => false,
      closed_day_2 => false,
      "#{closed_day_2}_orders_processing" => true)

    # Add holiday starting 1 day after the closed_date_2 and lasting for 1 day.
    FactoryBot.create(:holiday,
      start_date: (closed_date_2 + 1.day).to_s,
      end_date: (closed_date_2 + 1.day).to_s,
      inventory_pool_id: @inventory_pool.id)

    # Add holiday starting 2 days after the closed_date_2,
    # lasting for 1 day and processing orders.
    FactoryBot.create(:holiday,
      orders_processing: true,
      start_date: (closed_date_2 + 2.day).to_s,
      end_date: (closed_date_2 + 2.day).to_s,
      inventory_pool_id: @inventory_pool.id)

    # | date                  | open     | processing orders   | holiday |
    # | --------------------- | -------- | ------------------- | ------- |
    # | today                 | true     | true                | false   |
    # | today + 1 day         | false    | false               | false   |
    # | today + 2 days        | false    | true                | false   |
    # | today + 3 days        | false    | false               | true    |
    # | today + 4 days        | false    | true                | true    |
    # | today + 5 days        | true     | true                | false   |

    @inventory_pool.update(borrow_reservation_advance_days: 3)
  end
end
