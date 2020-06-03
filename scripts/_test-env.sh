export DATABASE_URL="postgresql://localhost:5432/leihs_test?max-pool-size=5" # for rails/DB
export LEIHS_DATABASE_URL="jdbc:${DATABASE_URL}"                             # for clj/server

export LEIHS_LEGACY_PORT=3210
export LEIHS_LEGACY_HTTP_BASE_URL="http://localhost:${LEIHS_LEGACY_PORT}"    # for server config

export LEIHS_BORROW_SPECIAL_PER_PAGE_DEFAULT=2
