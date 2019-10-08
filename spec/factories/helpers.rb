# This method takes a nested hash of factory data or an array
# of these as argument. It maps over and walks down the data
# structure and creates the factories respectively in the order
# of the traversal. It is thus important how one nests the
# associated entities. Some has to be created before the other,
# in order to establish the association (foreign keys dependencies).
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
