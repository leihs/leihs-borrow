RSpec.configure do |config|
  config.expect_with :rspec do |expectations|
    # This option will default to `true` in RSpec 4.
    expectations.include_chain_clauses_in_custom_matcher_descriptions = true
  end
  # This option will default to `:apply_to_host_groups` in RSpec 4
  config.shared_context_metadata_behavior = :apply_to_host_groups

  config.disable_monkey_patching!
  config.expose_dsl_globally = true

  config.warnings = false # sequel is just too noisy

  # Many RSpec users commonly either run the entire suite or an individual
  # file, and it's useful to allow more verbose output when running an
  # individual spec file.
  if config.files_to_run.one?
    # Use the documentation formatter for detailed output,
    # unless a formatter has already been configured
    # (e.g. via a command-line flag).
    config.default_formatter = "doc"
  end

  config.before :each do
    srand 1
  end



  # Turnip:
  config.raise_error_for_unimplemented_steps = true # TODO: fix

  config.before(type: :feature) do

    fp = self.class.superclass.file_path
    bn = File.basename(fp, '.feature')
    dn = File.dirname(fp)

    require_shared_files(dn)

    feature_steps_file = "#{dn}/#{bn}.steps.rb"
    require(feature_steps_file) if File.exist?(feature_steps_file)

    Capybara.current_driver = :firefox
    begin
      page.driver.browser.manage.window.resize_to(*BROWSER_WINDOW_SIZE)
    rescue => e
      page.driver.browser.manage.window.maximize
    end
  end

  config.before(pending: true) do |example|
    example.pending
  end

  config.after(type: :feature) do |example|
    if ENV['CIDER_CI_TRIAL_ID'].present?
      unless example.exception.nil?
        take_screenshot('tmp/error-screenshots')
      end
    end

    page.driver.quit # OPTIMIZE force close browser popups
    Capybara.current_driver = Capybara.default_driver
  end

  #
  config.after(:each) do |example|
    # auto-pry after failures, except in CI!
    unless ENV['CIDER_CI_TRIAL_ID'].present? or ENV['NOPRY']
      unless example.exception.nil?
        puts decorate_exception(example.exception)
        binding.pry if example.exception
      end
    end
  end
end


# require files from any shared folders down the directory path
def require_shared_files(dirpath)
  shared_folders = []

  dirpath.split("/").reduce do |acc, el|
    sub_dir = "#{acc}/#{el}"
    shared_folders.push "#{sub_dir}/shared"
    sub_dir
  end

  shared_folders.each do |sf|
    Dir.glob("#{sf}/*.rb") { |f| require f }
  end
end


def decorate_exception(ex)
  div = Array.new(80, '-').join
  msg = case true
  when ex.is_a?(Turnip::Pending)
    "MISSING STEP! try this:\n\n"\
    "step \"#{ex.message}\" do\n  binding.pry\nend"
  else
    "GOT ERROR: #{ex.class}: #{ex.message}"
  end
  "\n\n#{div}\n\n#{msg}\n\n#{div}\n\n"
end
