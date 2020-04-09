export DATABASE_URL="postgresql://localhost:5432/leihs_prod?max-pool-size=5" # for rails/DB
export LEIHS_DATABASE_URL="jdbc:${DATABASE_URL}"                             # for clj/server

export LEIHS_LEGACY_PORT=3210                                                # to start for dev
export LEIHS_LEGACY_HTTP_BASE_URL="http://localhost:${LEIHS_LEGACY_PORT}"    # for server config
