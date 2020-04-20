require 'timecop'

if fake_time_env = ENV['LEIHS_SPEC_FAKE_TIME']
  fake_time = Time.parse(fake_time_env)
  Timecop.freeze(fake_time)
  puts 'FAKE TIME set to: ' + fake_time_env
end
