require 'edn'
require 'pry'

module Constants
  LEIHS_DATABASE_URL = ENV['LEIHS_DATABASE_URL'].presence || 'leihs'
  LEIHS_PROCURE_HTTP_BASE_URL = ENV['LEIHS_PROCURE_HTTP_BASE_URL'].presence || 'http://localhost:3230'

  raise 'LEIHS_DATABASE_URL not set!' unless LEIHS_DATABASE_URL
  raise 'LEIHS_PROCURE_HTTP_BASE_URL not set!' unless LEIHS_PROCURE_HTTP_BASE_URL
end
