def wait_until(wait_time = 60, &block)
  begin
    Timeout.timeout(wait_time) do
      until value = yield
        sleep(1)
      end
      value
    end
  rescue Timeout::Error => _e
    fail Timeout::Error.new(block.source), 'It timed out!'
  end
end

def simulate_typing(el, val)
  val.chars.each { |c| el.send_keys(c); sleep(0.1) }
end
