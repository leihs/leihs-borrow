def factorize!(arg)
  case arg
  when Array
    arg.map { |x| factorize!(x) }
  when Hash
    factory = arg.delete(:factory)
    trait = arg.delete(:trait)
    raise 'No :factory key given!' unless factory
    attrs = arg.map { |k, v| [k, factorize!(v)] }.to_h
    if trait
      FactoryBot.create(factory, trait, attrs)
    else
      FactoryBot.create(factory, attrs)
    end
  else
    arg
  end
end
