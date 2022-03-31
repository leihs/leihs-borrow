step "I see the following text in the dialog:" do |txt|
  expect(@dialog).to be
  expect(@dialog).to have_content(txt.strip())
end

step "I see the :title dialog with the text:" do |title, text|
  within(find_ui_modal_dialog(title: title)) do
    expect(find(".modal-body").text).to eq text
  end
end

step "I see a warning in the dialog:" do |txt|
  expect(@dialog).to be
  within(@dialog) do
    err_msg = find(".invalid-feedback")
    scroll_to err_msg
    expect(err_msg.text).to eq txt
  end
end

step "the :title button is disabled" do |title|
  expect(find('button', text: title )).to be_disabled
end