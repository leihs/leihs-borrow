step 'the borrow app is loaded' do
  find("a[href='/app/borrow/']", text: 'Leihs')
end

step 'I visit legacy borrow' do
  visit("http://localhost:#{LEIHS_LEGACY_HTTP_PORT}/borrow")
end

step 'legacy borrow is loaded' do
  find("#start[href='/borrow']")
end
