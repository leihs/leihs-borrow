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

step "lending term acceptance is turned on in settings" do
  Settings.first.update(lending_terms_acceptance_required_for_order: true)
end

step "the :title dialog did not close" do |title|
  # Same as shared step "I see the :title dialog". Just so I can say "I click on the button, but the dialog did not close"
  dialog = find_ui_modal_dialog(title: title)
  expect(dialog).to be
end

step "contact details is turned on in settings" do
  Settings.first.update(show_contact_details_on_customer_order: true)
end
