require_relative "shared/refresh_timeout_data"
include ShoppingCartRefreshTimeoutData # standard:disable Style/MixinUsage

# NOTE: does not work because `let` from respec is not available.
#       GOT ERROR: NoMethodError: undefined method `let' for ShoppingCartRefreshTimeoutData:Module
# BUT if that problem were solved, we still would not have `let` inscope because we are not in a "before" block,
# even though we call it from a "background" step (but turnip still runs it inside "example", not in "before")
#       Spec::Core::ExampleGroup::WrongScopeError: `let` is not available from within an example (e.g. an `it` block) or from constructs that run in the scope of an example (e.g. `before`, `let`, etc). It is only available on an example group (e.g. a `describe` or `context` block).
#       from /Users/ma/.rbenv/versions/2.6.6/lib/ruby/gems/2.6.0/gems/rspec-core-3.9.3/lib/rspec/core/example_group.rb:757:in `method_missing'
step "the refresh timeout spec data is loaded" do
  # binding.pry
  ShoppingCartRefreshTimeoutData.load_data
end
