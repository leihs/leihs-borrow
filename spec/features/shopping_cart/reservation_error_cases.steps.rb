require_relative "./shared/refresh_timeout_data"
include ShoppingCartRefreshTimeoutData

# NOTE: does not work because `let` from respec is not available. either find a way to include it or give up…
#       GOT ERROR: NoMethodError: undefined method `let' for ShoppingCartRefreshTimeoutData:Module
step "the refresh timeout spec data is loaded" do
  ShoppingCartRefreshTimeoutData.load_data
end
