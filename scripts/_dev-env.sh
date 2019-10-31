export DATABASE_URL="postgresql://localhost:5432/leihs_prod?max-pool-size=5" 
export LEIHS_DATABASE_URL="jdbc:${DATABASE_URL}" 

export LEIHS_LEGACY_PORT=3000
export LEIHS_LEGACY_HTTP_BASE_URL="http://localhost:${LEIHS_LEGACY_PORT}"
