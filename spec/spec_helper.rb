require 'active_support/all'
require 'addressable'
require 'base32/crockford'
require 'capybara'
require 'uuidtools'
require 'pry'

ACCEPTED_FIREFOX_ENV_PATHS = ['FIREFOX_ESR_78_PATH']

# switch to HTTPS ?
LEIHS_BORROW_HTTP_BASE_URL = ENV['LEIHS_BORROW_HTTP_BASE_URL'].presence || 'http://localhost:3250'
LEIHS_BORROW_HTTP_PORT =  Addressable::URI.parse(LEIHS_BORROW_HTTP_BASE_URL).port.presence  || '3250'

raise 'LEIHS_BORROW_HTTP_BASE_URL not set!' unless LEIHS_BORROW_HTTP_BASE_URL

BROWSER_WINDOW_SIZE = [ 1200, 800 ]

Capybara.app_host = LEIHS_BORROW_HTTP_BASE_URL

require 'config/database'
require 'config/factories'
require 'config/metadata_extractor'
require 'config/hash'
require 'config/screenshots'
require 'config/features'
require 'config/browser'
require 'config/locales'
