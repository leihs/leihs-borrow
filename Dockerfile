ARG RUBY_VERSION=2.7.6-slim-bullseye
ARG NODEJS_VERSION=16-bullseye-slim
ARG JAVA_VERSION=11-slim-bullseye
ARG LEIHS_UI_VERSION=dev

# more build args in later stages:
# ARG WORKDIR=/leihs/borrow
# ARG LEIHS_UI_WORKDIR=/leihs-ui
# ARG GIT_COMMIT_ID=unknown-commit-id
# ARG GIT_TREE_ID=unknown-tree-id

# === STAGE: BASE JAVA ========================================================================== #
FROM openjdk:${JAVA_VERSION} as leihs-base-java

# === STAGE: BASE RUBY ========================================================================== #
FROM ruby:${RUBY_VERSION} as leihs-base-ruby

# === STAGE: BASE NODEJS ======================================================================== #
FROM node:${NODEJS_VERSION} as leihs-base-nodejs

# === STAGE: SHARED UI ========================================================================== #
FROM leihs-ui:$LEIHS_UI_VERSION as leihs-ui-dist

# === STAGE: BUILD UBERJAR ====================================================================== #
FROM leihs-base-java as builder

ARG WORKDIR=/leihs/borrow
WORKDIR "$WORKDIR"

# OS deps
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        git && \
    rm -rf /var/lib/apt/lists/*

# "merge" in nodejs installation from offical image (matching debian version).
COPY --from=leihs-base-nodejs /usr /usr/
# smoke check
RUN node --version && npm --version

# "merge" in ruby installation from offical image (matching debian version).
COPY --from=leihs-base-ruby /usr /usr/
RUN echo 'gem: --no-document' >> /usr/local/etc/gemrc
# smoke check
RUN ruby --version && echo "GEM: $(gem --version)" && bundle --version

# prepare clojure dependencies
# `clojure -X:deps prep`        -> Prepare all unprepped libs in the dep tree
# `clojure -T:build-leihs prep` -> hack, throws an error, but download deps before that ;)
ENV PATH=${WORKDIR}/shared-clj/clojure/bin:$PATH
COPY deps.edn .
COPY scripts/build.clj .
COPY shared-clj shared-clj/
RUN clojure -X:deps prep && \
    ( clojure -T:build-leihs prep || true )
# clojurescript / shadow-cljs
COPY shadow-cljs.edn package.json package-lock.json ./
RUN npm ci --no-audit --loglevel=warn
RUN npx shadow-cljs i

# copy sources and shared/prebuilt dependencies
ARG LEIHS_UI_WORKDIR=/leihs-ui
COPY --from=leihs-ui-dist ${LEIHS_UI_WORKDIR}/dist leihs-ui/dist/
COPY --from=leihs-ui-dist ${LEIHS_UI_WORKDIR}/bootstrap-theme-leihs/build leihs-ui/bootstrap-theme-leihs/build/
COPY --from=leihs-ui-dist ${LEIHS_UI_WORKDIR}/bootstrap-theme-leihs-mobile/build leihs-ui/bootstrap-theme-leihs-mobile/build/
COPY . .

# BUILD: see bin/build (`function build`) for the steps, leaving out those that are prepared (leihs ui, dependencies, …)
# NOTE: could be further (docker-cache-)optimized by only copying the needed sources before build, but that seems brittle (e.g. copy src/client first, then build client…)
RUN INSTALL_DEPS=NO ./bin/cljs-release
RUN ./bin/dump-translations
RUN ./bin/clj-uberjar
# NOTE: git info comes last, because the commit-id acts a cache-buster. therefore all expensive tasks need to be done before!
#       also, we dont run git inside the container (needs full repo inside), but instead supply needed info etc via build arg/env vars:
ARG GIT_COMMIT_ID=unknown-commit-id
ARG GIT_TREE_ID=unknown-tree-id
RUN BUILD_COMMIT_ID="${GIT_COMMIT_ID}" BUILD_TREE_ID="${GIT_TREE_ID}" ./bin/set-built-info
RUN jar uf target/leihs-borrow.jar resources/built-info.yml

# === STAGE: PROD IMAGE ========================================================================= #
FROM leihs-base-java
ARG WORKDIR=/leihs/borrow
WORKDIR "$WORKDIR"

# "merge" in nodejs installation from offical image (matching debian version).
COPY --from=leihs-base-nodejs /usr /usr/
# smoke check
RUN node --version && npm --version

COPY --from=builder ${WORKDIR}/target/leihs-borrow.jar target/

# config
ENV HTTP_PORT=3250

# run
EXPOSE ${HTTP_PORT}
ENTRYPOINT [ "java", "-jar", "target/leihs-borrow.jar", "run" ]