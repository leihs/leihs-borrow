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

  config.before(type: :feature) do |example|
    feature_file_absolute = absolute_feature_file(example)
    require_shared_files feature_file_absolute
    require_feature_steps feature_file_absolute
    Capybara.current_driver = :firefox
  end

  config.before(pending: true) do |example|
    example.pending
  end

  config.after(type: :feature) do |example|
    if ENV["CIDER_CI_TRIAL_ID"].present?
      unless example.exception.nil?
        take_screenshot("tmp/error-screenshots")
      end
    end
    page.driver.quit
    Capybara.current_driver = Capybara.default_driver
  end

  config.after(:each) do |example|
    # auto-pry after failures, except in CI!
    if !ENV["CIDER_CI_TRIAL_ID"].present? && ENV["PRY_ON_EXCEPTION"].present?
      unless example.exception.nil?
        puts decorate_exception(example.exception)
        binding.pry if example.exception # standard:disable Lint/Debugger
      end
    end
  end
end

def absolute_feature_file example
  feature_file_absolute = Pathname.new(example.file_path).expand_path
  unless feature_file_absolute.exist?
    msg = <<~ERR.strip
      feature_file_absolute #{feature_file_absolute} must exist and be absolute
    ERR
    raise msg
  end
  feature_file_absolute
end

def require_feature_steps(feature_file_absolute)
  feature_steps_file = feature_file_absolute.sub_ext(".steps.rb")
  require(feature_steps_file) if feature_steps_file.exist?
end

def require_shared_files(feature_file_absolute)
  features_dir = Pathname.pwd.join("spec", "features")
  relative_dirs_to_feature_file = feature_file_absolute.relative_path_from(features_dir)
  ([features_dir] + relative_dirs_to_feature_file.to_s.split(File::Separator)).reduce do |current_dir, sub|
    current_dir.join("shared").glob("**/*.rb").each do |ruby_file|
      require(ruby_file)
    end
    current_dir.join(sub)
  end
end

def decorate_exception(ex)
  div = Array.new(80, "-").join
  location = begin ex.backtrace.first; rescue; end
  msg = case true # standard:disable Lint/LiteralAsCondition
  when ex.is_a?(Turnip::Pending)
    "MISSING STEP! try this:\n\n" \
    "step \"#{ex.message}\" do\n  binding.pry\nend"
  else
    "GOT ERROR: #{ex.class}: #{ex.message}"
  end
  trace = unless ex.is_a?(RSpec::Expectations::ExpectationNotMetError)
    "from: #{location || "UNKNOWN"}"
  end
  "\n\n#{div}\n\n#{msg}\n\n#{div}\n#{trace}\n"
end

def log_turnip_step(file, step)
  indent = " " * 3
  inner_indent = (indent * 2) + (" " * 5)
  line_nr = step.line.to_s.rjust(3, " ")
  step_text = "#{step.keyword}#{step.text}"
  args = if step.argument.is_a?(Turnip::Table)
    table = step.argument
    json_line = table.hashes.to_json
    if json_line.length < 120
      "\n#{inner_indent}#{json_line}"
    else
      dat = JSON.pretty_generate(table.hashes).split("\n")
        .map { |l| "#{inner_indent}#{l}" }.join("\n")
      "\n#{dat}"
    end
  end
  puts "#{indent}#{line_nr} | #{step_text}#{args}"
end

# monkey-patch Turnip
module TurnipExtensions
  module CustomStepRunner
    def run_step(*args)
      log_turnip_step(*args)
      super
    end
  end
end

Turnip::RSpec::Execute.prepend TurnipExtensions::CustomStepRunner
