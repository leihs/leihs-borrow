step "I see the following text in the dialog:" do |txt|
  expect(@dialog).to be
  expect(@dialog).to have_content(txt.strip())
end

step "I see the :title dialog with the text:" do |title, text|
  within(find_ui_modal_dialog(title: title)) do
    expect(find(".modal-body").text).to eq text
  end
end