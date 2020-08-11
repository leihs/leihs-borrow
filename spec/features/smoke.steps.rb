step 'the borrow app is loaded' do
  find("a[href='/app/borrow/']", text: 'LEIHS')
end

step 'I visit legacy borrow' do
  visit("http://localhost:#{ENV['LEIHS_LEGACY_HTTP_PORT']}/borrow")
end

step 'legacy borrow is loaded' do
  find("#start[href='/borrow']")
end
