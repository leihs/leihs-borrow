#!/bin/sh -exu

java -version 2>&1 | grep -E '(java|openjdk) version "1.8.' || {
  echo "FATAL: this must be build with JDK 8! See issue for details: <https://github.com/leihs/leihs/issues/756>"
  exit 1
}

npm ci || npm i
npx shadow-cljs release app

export LEIN_SNAPSHOTS_IN_RELEASE=Yes
bin/boot show -e
bin/boot uberjar
