def factorise!(arg)
  case arg
  when Array
    arg.map { |el| factorise_helper!(el) }
  when Hash
    factorise_helper!(arg)
  else
    raise 'wtf'
  end
end

def factorise_helper!(arg, strategy = :create)
  associations = arg.delete(:associations)
  factory = arg.delete(:factory)
  raise 'No :factory key given!' unless factory
  trait = arg.delete(:trait)

  object = FactoryBot.send(strategy, factory, trait, arg)

  associations.try(:each_pair) do |key, value|
    factorised_value = case value
                       when Array
                         value.map do |v|
                           factorise_helper!(v, :build)
                         end
                       when Hash
                         factorise_helper!(value, :build)
                       else
                         raise 'wtf'
                       end

    Array
      .wrap(factorised_value)
      .each do |fv|
        object.send("add_#{key.to_s.singularize}", fv)
      end
  end

  object
end
