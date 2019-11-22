export PROJECT_DIR="$LEIHS_BORROW_DIR"
export UBERJAR_NAME="leihs-borrow"
LEIN_UBERJAR_PATH="${PROJECT_DIR}/target/${UBERJAR_NAME}.jar" # path of the expeced repository uberjar path

function build_uberjar() {
  echo "INFO: building the ${UBERJAR_NAME} uberjar now"
  cd $LEIHS_BORROW_DIR
  export LEIN_SNAPSHOTS_IN_RELEASE=yes
  $PROJECT_DIR/bin/boot show -e
  $PROJECT_DIR/bin/boot uberjar
}
