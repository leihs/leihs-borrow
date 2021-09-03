step "I have one item in the cart ready to be submitted" do
  item = FactoryBot.create(:item, owner: @pool, responsible: @pool)
  FactoryBot.create(:reservation,
    status: :unsubmitted,
    inventory_pool: @pool,
    user: @user,
    start_date: Date.tomorrow,
    end_date: Date.tomorrow + 1.day,
    leihs_model: item.leihs_model)
end

step "I navigate to the cart" do
  visit "/app/borrow/order"
end

step "I enter :txt as title" do |txt|
  find_field("Title").set(txt)
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

step "I have been redirected to the newly created order" do
  @order = Order.order(Sequel.desc(:created_at)).first
  expect(current_path).to eq "/app/borrow/rentals/#{@order.id}"
end

step "the newly created order has title :txt" do |txt|
  expect(page).to have_content "\"title\": \"#{txt}\""
end

step "the newly created order has purpose :txt" do |txt|
  expect(page).to have_content "\"purpose\": \"#{txt}\""
end
