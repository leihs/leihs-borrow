def wait_until(wait_time = 60, &block)
  begin
    Timeout.timeout(wait_time) do
      until value = yield
        sleep(1)
      end
      value
    end
  rescue Timeout::Error => _e
    fail Timeout::Error.new(block.source), "It timed out!"
  end
end

def simulate_typing(el, val)
  val.chars.each { |c| el.send_keys(c); sleep(0.1) }
end

def log_in_as_user_with_email(email)
  user = User.find(email: email)
  expect(user).to be
  visit "/app/borrow/"
  within(".ui-form-signin") do
    fill_in("user", with: email)
    find('button[type="submit"]').click
  end
  within(".ui-form-signin") do
    fill_in("password", with: "password")
    find('button[type="submit"]').click
  end
  user
end

# spec args given as "${some_ruby_code}" -> eval(some_ruby_code)
def custom_eval(spec_string)
  ruby_code = spec_string.to_s.match(/^\$\{(.*)\}$/)[1]
  eval(ruby_code)
end

# spec args given as "A${1+1}Z" -> "A#{1+1}Z" -> "A2Z"
def custom_interpolation(spec_string, format_func = ->(x) { x })
  spec_string.gsub(/\$\{([^\$]*)\}/) do |s|
    format_func.call(custom_eval(s))
  end
end
