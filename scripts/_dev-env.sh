# read in local env file if it exists

SCRIPT_FULL_PATH=$(realpath "$0")
SCRIPT_DIR=$(dirname $SCRIPT_FULL_PATH)
ENV_FILE_PATH=$SCRIPT_DIR/../.env.local

test -f $ENV_FILE_PATH && . $ENV_FILE_PATH

export DATABASE_URL="${DATABASE_URL:-"postgresql://localhost:5432/leihs?max-pool-size=5"}" # for rails/DB
export LEIHS_DATABASE_URL="jdbc:${DATABASE_URL}"                             # for clj/server

export LEIHS_LEGACY_PORT=3210                                                # to start for dev
export LEIHS_LEGACY_HTTP_BASE_URL="http://localhost:${LEIHS_LEGACY_PORT}"    # for server config

export LEIHS_BORROW_LOAD_TRANSLATIONS=true
