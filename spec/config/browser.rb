require "pry"
require "capybara"
require "capybara/rspec"
require "selenium-webdriver"
require "turnip/capybara"
require "turnip/rspec"

firefox_bin_path = if ENV["TOOL_VERSIONS_MANAGER"] == "mise"
  Pathname.new(`mise where firefox`.strip).join("bin/firefox").expand_path.to_s
else
  Pathname.new(`asdf where firefox`.strip).join("bin/firefox").expand_path.to_s
end
Selenium::WebDriver::Firefox.path = firefox_bin_path

LEIHS_BORROW_HTTP_BASE_URL = ENV["LEIHS_BORROW_HTTP_BASE_URL"].presence || "http://localhost:3250"
LEIHS_BORROW_HTTP_PORT = Addressable::URI.parse(LEIHS_BORROW_HTTP_BASE_URL).port.presence || "3250"
raise "LEIHS_BORROW_HTTP_BASE_URL not set!" unless LEIHS_BORROW_HTTP_BASE_URL
Capybara.app_host = LEIHS_BORROW_HTTP_BASE_URL

Capybara.register_driver :firefox do |app|
  Selenium::WebDriver::Remote::Capabilities.firefox(
    # TODO: trust the cert used in container and remove this:
    acceptInsecureCerts: true
  )

  profile = Selenium::WebDriver::Firefox::Profile.new
  # TODO: configure language for locale testing
  # profile["intl.accept_languages"] = "en"

  opts = Selenium::WebDriver::Firefox::Options.new(
    binary: firefox_bin_path,
    profile: profile,
    log_level: :trace
  )

  # NOTE: good for local dev
  if ENV["LEIHS_TEST_HEADLESS"].present?
    opts.args << "--headless"
  end
  # opts.args << '--devtools' # NOTE: useful for local debug

  Capybara::Selenium::Driver.new(app, browser: :firefox, options: opts)
end

Capybara.default_driver = :firefox
Capybara.current_driver = :firefox

Capybara.configure do |config|
  config.default_max_wait_time = 15
end
