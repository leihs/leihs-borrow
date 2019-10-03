def factorize!(arg)
  case arg
  when Array
    arg.map { |x| factorize!(x) }
  when Hash
    factory = arg.delete(:factory)
    raise 'No :factory key given!' unless factory
    attrs = arg.map { |k, v| [k, factorize!(v)] }.to_h
    FactoryBot.create(factory, attrs)
  else
    arg
  end
end
