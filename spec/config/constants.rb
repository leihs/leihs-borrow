require 'edn'
require 'pry'

module Constants
  LEIHS_DATABASE_URL = ENV['LEIHS_DATABASE_URL'].presence || 'leihs'
  LEIHS_BORROW_HTTP_BASE_URL = ENV['LEIHS_BORROW_HTTP_BASE_URL'].presence || 'http://localhost:3250'

  raise 'LEIHS_DATABASE_URL not set!' unless LEIHS_DATABASE_URL
  raise 'LEIHS_BORROW_HTTP_BASE_URL not set!' unless LEIHS_BORROW_HTTP_BASE_URL
end
