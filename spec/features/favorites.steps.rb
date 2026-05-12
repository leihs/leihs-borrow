step "I see :n favorite(s)" do |n|
  wait_until do
    all(".ui-models-list a.stretched-link").count == n.to_i
  end
end

step "I see model :name" do |name|
  find(".ui-models-list a.stretched-link", text: name)
end
