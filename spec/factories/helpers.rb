# This method takes a nested hash of factory data or an array
# of these as argument. It maps over and walks down the data
# structure and creates the factories respectively in the order
# of the traversal. It is thus important how one nests the
# associated entities. Some has to be created before the other,
# in order to establish the association (foreign keys dependencies).

def factorise!(arg, strategy = :create)
  associations = arg.delete(:associations)
  factory = arg.delete(:factory)
  raise 'No :factory key given!' unless factory
  trait = arg.delete(:trait)

  object = FactoryBot.send(strategy, factory, trait, arg)

  associations.try(:each_pair) do |key, value|
    factorised_value = case value
                       when Array
                         value.map do |v|
                           factorise!(v, :build)
                         end
                       when Hash
                         factorise!(value, :build)
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
