step "the :name field has :value" do |name, value|
  expect(find_field(name).value).to eq value.to_s
end

step "I wait :num seconds" do |time|
  sleep time.to_f
end

step "I see a form inside the dialog" do
  expect(@dialog).to be
  @form = @dialog.find("form")
  expect(@form).to be
end

step "the form has an error message:" do |txt|
  expect(@form).to be
  within(@form) do
    err_msg = find(".invalid-feedback")
    scroll_to err_msg
    expect(err_msg.text).to eq txt
  end
end

step "the form has no error message" do
  expect(@form).to be
  within(@form) { expect(page).to have_no_selector ".invalid-feedback" }
end

step "the form has exactly these fields:" do |table|
  form_fields = within @form do
    all("section").map do |sec|
      sec.all("input,textarea,select", wait: 0).map do |field|
        field_id = field[:id]
        label = if field_id
            find("label[for='#{field_id}']", wait: 0)
          else
            field.find(:xpath, "./ancestor::label")
          end
        value = if field.tag_name === "select"
            field.find("option[value='#{field.value}']").text
          else
            field.value
          end
        { label: label.text, value: value }
      end
    end
  end.flatten

  expect(form_fields).to eq(symbolize_hash_keys(table.hashes))
end
