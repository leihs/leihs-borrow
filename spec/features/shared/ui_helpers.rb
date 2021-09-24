# UI Helpers
#
# for specific *Design Components* from `leihs-ui`
#
# conventions:
# * methods `find_ui_EXAMPLE` to return component EXAMPLE like capybara `find` would
# * methods `get_ui_EXAMPLE` to return component EXAMPLE serialized as a hash for data-based spec expectations
# * no "app specific" matchers that "know to much" about the data presented in the UI.
#   * good: match (card) lines, serialize to `{title: "Foo", body: "Bar"}`
#   * bar: match reservation line items, serialize to `{model: "Foo", quantity: "Bar"}`
#
# TODO:
# * should move into `leihs-ui` repo and also be tested there (run against storybook)
# * helpers to *interact* with components, e.g. open/close collapsible sections, navigate the calendar…

# `Section` <https://ui.leihs.app/?path=/story/mobileapp-design-components-content-section>
def find_ui_section(title:)
  # NOTE: `@class=foo` does not work for `class="foo bar"` http://pivotallabs.com/xpath-css-class-matching/
  xps =
    "//*[contains(concat(' ',normalize-space(@class),' '), ' section-title ') " \
    "and contains(., '#{title}')]/ancestor-or-self::section"
  find(:xpath, xps)
end

# `ModalDialog` <https://ui.leihs.app/?path=/story/mobileapp-design-components-layout-modaldialog>
def find_ui_modal_dialog(title:, present: true)
  xps =
    "//div[contains(concat(' ',normalize-space(@class),' '), ' modal-dialog ')]" \
    "[//*[contains(concat(' ',normalize-space(@class),' '), ' modal-title ') and contains(., '#{title}')]]"
  if present
    find(:xpath, xps)
  else
    page.has_no_selector?(:xpath, xps)
  end
end

# `ListCard` <https://ui.leihs.app/?path=/story/mobileapp-design-components-content-listcard>
def find_ui_list_cards(scope = page)
  scope.all(".ui-list-card")
end

def find_ui_list_card(**args)
  find(".ui-list-card", **args)
end

def get_ui_list_cards(scope = page)
  find_ui_list_cards(scope).map do |c|
    divs = c.all(":scope > div")
    {
      link: c.find(":scope > a")[:href],
      title: divs[0].text,
      body: divs[1].text,
      foot: divs[2].text,
    }
  end
end

def find_ui_page_layout
  find(".ui-page-layout")
end

def get_ui_page_layout
  within(find_ui_page_layout) do
    {
      title: first("h1").text,
      subtitle: first("h2").text,
    }
  end
end

# ruby helpers

def symbolize_hash_keys(o)
  return o.map { |i| symbolize_hash_keys(i) } if o.is_a?(Array)
  o.values.select { |v| v.is_a?(Hash) }.map { |h| symbolize_hash_keys(h) }
  o.map { |k, v| [k.to_sym, v.is_a?(Hash) ? symbolize_hash_keys(o) : v] }.to_h
end

# get expectation tables from given UI,
# ex. $ puts hashes_to_gherkin_table(get_ui_list_cards)
def hashes_to_gherkin_table(input)
  arr = input.is_a?(Hash) ? [input] : input
  sep = " | "
  keys = arr.map(&:keys).flatten.uniq
  cols = keys.map(&:to_s)
  rows = arr.map do |row| keys.map { |c| row[c].to_s } end
  [cols].concat(rows).map do |line|
    [nil].push(line).push(nil).join(sep).strip
  end.join("\n")
end
