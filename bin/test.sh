#!/usr/bin/env bash
set -eux
source shared-clj/clojure/bin/activate
export FILE=./bin/translations-check.sql
./bin/build-translations
if ! cmp -s bin/translations.sql $FILE; then
  echo "File bin/translations.sql is not up-to-date. Execute bin/build-translations"
fi
