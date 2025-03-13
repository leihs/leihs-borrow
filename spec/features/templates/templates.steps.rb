step "I see the following categories:" do |table|
  cat_list = find(".ui-category-list")
  cat_items = cat_list.all(".ui-square-image-grid-item")
  captions = cat_items.map { |item| item.find(".ui-caption", wait: false).text }

  expected_captions = table.hashes.map { |h| h["caption"] }
  expect(captions).to eq expected_captions
end

def get_category_item_by_caption(caption)
  cat_list = find(".ui-category-list")
  cat_list.all(".ui-square-image-grid-item").find do |item|
    c = item.find(".ui-caption", wait: false).text
    c == caption
  end
end

step "I click on the item captioned :caption in the category list" do |caption|
  cat_item = get_category_item_by_caption(caption)
  expect(cat_item).to be
  cat_item.click
end

step "I see the following models:" do |table|
  models_list = find(".ui-models-list")
  model_items = models_list.all(".ui-square-image-grid-item")
  captions = model_items.map { |item| item.find(".ui-caption", wait: false).text }

  expected_captions = table.hashes.map { |h| h["caption"] }
  expect(captions).to eq expected_captions
end

step "I see the following text in the dialog:" do |txt|
  expect(@dialog).to be
  expect(@dialog).to have_content(txt.strip)
end

step "I see the :title dialog with the text:" do |title, text|
  within(find_ui_modal_dialog(title: title)) do
    expect(find(".modal-body").text).to eq text
  end
end
