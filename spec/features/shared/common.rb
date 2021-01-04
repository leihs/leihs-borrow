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
