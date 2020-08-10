require 'pry'
require 'capybara/rspec'
require 'selenium-webdriver'
require 'turnip/capybara'
require 'turnip/rspec'



def accepted_firefox_path 
  ENV[ ACCEPTED_FIREFOX_ENV_PATHS.detect do |env_path|
    ENV[env_path].present?
  end || ""].tap { |path|
    path.presence or raise "no accepted FIREFOX found"
  }
end

Selenium::WebDriver::Firefox.path = accepted_firefox_path



Capybara.register_driver :firefox do |app|
  capabilities = Selenium::WebDriver::Remote::Capabilities.firefox(
    # TODO: trust the cert used in container and remove this:
    acceptInsecureCerts: true
  )

  profile = Selenium::WebDriver::Firefox::Profile.new
  # TODO: configure language for locale testing
  # profile["intl.accept_languages"] = "en"

  opts = Selenium::WebDriver::Firefox::Options.new(
    binary: ENV['FIREFOX_ESR_60_PATH'],
    profile: profile,
    log_level: :trace)

  # NOTE: good for local dev
  if ENV['LEIHS_TEST_HEADLESS'].present?
    opts.args << '--headless'
  end
  # opts.args << '--devtools' # NOTE: useful for local debug

  # driver = Selenium::WebDriver.for :firefox, options: opts
  # Capybara::Selenium::Driver.new(app, browser: browser, options: opts)
  Capybara::Selenium::Driver.new(
    app,
    browser: :firefox,
    options: opts,
    desired_capabilities: capabilities
  )
end

# Capybara.run_server = false
Capybara.default_driver = :firefox
Capybara.current_driver = :firefox

Capybara.configure do |config|
  config.default_max_wait_time = 15
end
