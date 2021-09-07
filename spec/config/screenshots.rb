require 'capybara/rspec'

def doc_screenshot(name)
  fail unless name.is_a?(String) and name.present?
  take_screenshot('tmp/doc-screenshots', name)
end

def spec_screenshot(example, step)
  fail unless example.present?
  dat = example.metadata
  dir = "tmp/spec-screenshots/#{dat[:file_path]}"
  stepname = step.text.gsub(/\W/, ' ').gsub(/\s+/, ' ').strip.gsub(' ', '-')
  name = "#{dat[:example_group][:scoped_id].gsub(':', '_')}_#{step.location.line}_#{stepname}"
  take_screenshot(dir, name)
end

def take_screenshot(screenshot_dir = nil, name = nil)
  if !screenshot_dir.present?
    fail 'no `screenshot_dir` given!' unless defined?(Rails)
    screenshot_dir = Rails.root.join('tmp', 'capybara')
  end

  name ||= "screenshot_#{DateTime.now.utc.iso8601.tr(':', '-')}"
  name = "#{name}.png" unless name.ends_with?('.png')

  path = File.join(Dir.pwd, screenshot_dir, name)
  FileUtils.mkdir_p(File.dirname(path))

  case Capybara.current_driver
  when :firefox
    page.driver.browser.save_screenshot(path)
  else
    fail "Taking screenshots is not implemented for \
              #{Capybara.current_driver}."
  end
end

RSpec.configure do |config|
  config.after(type: :feature) do |example|
  end
end
