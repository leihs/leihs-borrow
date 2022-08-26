IP_UUID = "6bf7dc96-2b11-43c1-9f49-c58a5b332517"

step "there is a user" do
  @user = FactoryBot.create(:user)
end

step "there is a user :full_name" do |full_name|
  first, last = full_name.split
  login = user_login_from_full_name(full_name)
  email = Faker::Internet.email(name: full_name)
  @user = User.find(login: login) || FactoryBot.create(:user, firstname: first, lastname: last, login: login, email: email)
end

step "there is a delegation :name" do |name|
  @delegation = FactoryBot.create(:delegation, name: name)
end

step "the user is member of delegation :name" do |name|
  d = User.find(firstname: name)
  d.add_delegation_user(@user)
end

step "there is a user without password" do
  @user = FactoryBot.create(:user_without_password)
end

step "there is an initial admin" do
  @initial_admin = FactoryBot.create(:user, is_admin: true, admin_protected: true,
                                     is_system_admin: true, system_admin_protected: true)
end

step "there is a leihs admin" do
  @leihs_admin = FactoryBot.create(:user, is_admin: true, admin_protected: true)
end

step "there is a procurement admin" do
  @procurement_admin = FactoryBot.create(:procurement_admin).user
end

step "the user is leihs admin" do
  User.where(id: @user.id).update(is_admin: true, admin_protected: true)
end

step "there is a user with an ultimate access" do
  @user = FactoryBot.create(:user, is_admin: true, admin_protected: true,
                            is_system_admin: true, system_admin_protected: true)
  ip = FactoryBot.create(:inventory_pool, id: IP_UUID)
  FactoryBot.create(:procurement_admin, user_id: @user.id)
  FactoryBot.create(:direct_access_right,
                    user: @user,
                    inventory_pool: ip,
                    role: :inventory_manager)
end

step "the user does not have any pool access rights" do
  AccessRight.where(user_id: @user.id).delete
end

step "there is a default language :lang with locale name :l_name" do |lang, l_name|
  unless Language.find(name: lang, default: true)
    FactoryBot.create(:language,
                      name: lang,
                      default: true,
                      locale: l_name)
  end
end

step "there is a language :lang with locale name :l_name" do |lang, l_name|
  unless Language.find(name: lang)
    FactoryBot.create(:language,
                      name: lang,
                      locale: l_name)
  end
end

step "the user is customer of some pool" do
  FactoryBot.create(:direct_access_right, user_id: @user.id, role: :customer)
end

step "the user is inventory manager of some pool" do
  @pool = FactoryBot.create(:inventory_pool)
  FactoryBot.create(:direct_access_right,
                    user_id: @user.id,
                    inventory_pool_id: @pool.id,
                    role: :inventory_manager)
end

step "the user is inventory manager of pool :name" do |name|
  pool = InventoryPool.find(name: name) ||
         FactoryBot.create(:inventory_pool, name: name)

  FactoryBot.create(:direct_access_right,
                    user_id: @user.id,
                    inventory_pool_id: pool.id,
                    role: :inventory_manager)
end

def user_is_customer_of_pool(user, pool)
  pool = InventoryPool.find(name: pool) ||
         FactoryBot.create(:inventory_pool, name: pool)

  FactoryBot.create(:direct_access_right,
                    user_id: user.id,
                    inventory_pool_id: pool.id,
                    role: :customer)
end

step "the user is customer of pool :pool" do |pool|
  expect(@user).to be_a User
  user_is_customer_of_pool(@user, pool)
end

step "the user :full_name is customer of pool :pool" do |name, pool|
  user = find_user_by_full_name!(name)
  user_is_customer_of_pool(user, pool)
end

step "the delegation is customer of pool :pool" do |pool|
  expect(@delegation).to be_a User
  user_is_customer_of_pool(@delegation, pool)
end

step "the delegation :delegation is customer of pool :name" do |delegation, name|
  pool = InventoryPool.find(name: name) ||
         FactoryBot.create(:inventory_pool, name: name)

  FactoryBot.create(:direct_access_right,
                    user_id: @delegation.id,
                    inventory_pool_id: pool.id,
                    role: :customer)
end

step "the user is group manager of pool :name" do |name|
  pool = FactoryBot.create(:inventory_pool, name: name)
  FactoryBot.create(:direct_access_right,
                    user_id: @user.id,
                    inventory_pool_id: pool.id,
                    role: :group_manager)
end

step "the user is procurement admin" do
  FactoryBot.create(:procurement_admin, user_id: @user.id)
end

step "the user is procurement requester" do
  FactoryBot.create(:procurement_requester, user_id: @user.id)
end

step "there is an external authentication system" do
  @external_authentication_system =
    FactoryBot.create(:authentication_system, :external)
end

step "the user has external authentication" do
  ext_sys = AuthenticationSystem.find(type: "external")
  FactoryBot.create(:authentication_system_user,
                    user_id: @user.id,
                    authentication_system_id: ext_sys.id)
end

step "the user does not have password authentication" do
  AuthenticationSystemUser
    .where(user_id: @user.id, authentication_system_id: "password")
    .delete
end

step "the user has no access whatsoever" do
  AccessRight.where(user_id: @user.id).delete
  User.where(id: @user.id).update(is_admin: false)
  ProcurementRequester.where(user_id: @user.id).delete
  ProcurementAdmin.where(user_id: @user.id).delete
  ProcurementInspector.where(user_id: @user.id).delete
  ProcurementViewer.where(user_id: @user.id).delete
end

step "there is a supplier :supplier" do |supplier|
  FactoryBot.create(:supplier, name: supplier)
end

step "there is a building :building" do |building|
  FactoryBot.create(:building, name: building)
end

step "there is a room :room for building :building" do |room, building|
  b = Building.find(name: building)
  FactoryBot.create(:room, name: room, building: b)
end

step "there is an inventory pool :name" do |name|
  @pool = FactoryBot.create(:inventory_pool, name: name)
end

step "there is a model :name" do |name|
  FactoryBot.create(:leihs_model, product: name)
end

step "there is an option :name" do |name|
  FactoryBot.create(:option, product: name)
end

step "there are meta mail templates" do
  Language.all.each do |lang|
    [[:approved, :order],
     [:received, :order],
     [:rejected, :order],
     [:submitted, :order],
     [:deadline_soon_reminder, :user],
     [:reminder, :user]].each do |tmpl, type|
      FactoryBot.create(:mail_template,
                        is_template_template: true,
                        language_locale: lang.locale,
                        name: tmpl,
                        type: type,
                        format: :text,
                        body: "template stub",
                        created_at: DateTime.now,
                        updated_at: DateTime.now)
    end
  end
end

step "there is/are :n borrowable item(s) for model :model in pool :pool" do |n, model, pool|
  model = LeihsModel.find(product: model)
  pool = InventoryPool.find(name: pool)

  n.to_i.times do
    FactoryBot.create(:item,
                      is_borrowable: true,
                      leihs_model: model,
                      responsible: pool,
                      owner: pool)
  end
end

step "there is a category :name" do |name|
  @category = FactoryBot.create(:category, name: name)
end

step "the :model_name model belongs to category :cat_name" do |model_name, cat_name|
  cat = Category.find(name: cat_name)
  model = LeihsModel.find(product: model_name)
  cat.add_direct_model(model)
end

step "parent of category :child_name is category :parent_name" do |child_name, parent_name|
  parent = Category.find(name: parent_name)
  child = Category.find(name: child_name)
  parent.add_child(child)
end

def user_login_from_full_name(full_name)
  full_name.downcase.gsub(' ','')
end

def find_user_by_full_name!(name)
  User.find(login: user_login_from_full_name(name)) || fail("Could not find User '#{name}'!")
end
