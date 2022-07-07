step "I have one item in the cart ready to be submitted" do
  item = FactoryBot.create(:item, owner: @pool, responsible: @pool)
  FactoryBot.create(:reservation,
                    status: :unsubmitted,
                    inventory_pool: @pool,
                    user: @user,
                    start_date: Date.today,
                    end_date: Date.tomorrow,
                    leihs_model: item.leihs_model)
end

step "I enter :txt as :field" do |txt, field|
  find_field(field).set(txt)
end

step "I enter :txt as purpose" do |txt|
  find_field("Purpose").set(txt)
end

step "I submit the form" do
  find_field("Title").native.send_keys(:return)
end

step "the title contains :txt" do |txt|
  expect(find_field("Title").value).to eq txt
end

step "the purpose contains :txt" do |txt|
  expect(find_field("Purpose").value).to eq txt
end

step "I accept the :title dialog with the text:" do |title, text|
  within(find_ui_modal_dialog(title: title)) do
    expect(find(".modal-body").text).to eq interpolate_dates_short(text)
    click_on "OK"
  end
end
