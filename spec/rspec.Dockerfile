ARG RUBY_VERSION=2.7.6-bullseye
ARG NODEJS_VERSION=16-bullseye-slim

ARG WORKDIR=/leihs/borrow

# === STAGE: BASE RUBY ========================================================================== #
FROM ruby:${RUBY_VERSION} as leihs-base-ruby

# === STAGE: BASE NODEJS ======================================================================== #
FROM node:${NODEJS_VERSION} as leihs-base-nodejs

# === STAGE: RSPEC ============================================================================== #
FROM leihs-base-ruby

# install deps
RUN apt-get update -yqq && \
    apt-get install -yqq --no-install-recommends \
      git nodejs postgresql-client && \
    rm -rf /var/lib/apt/lists/*

# prepare system
RUN echo "gem: --no-rdoc --no-ri" >> ~/.gemrc
ENV BUNDLE_PATH /gems

RUN gem update --system
RUN gem install bundler

# install gems
COPY Gemfile Gemfile.lock ./
COPY database/Gemfile database/Gemfile.lock database/
RUN bundle install

WORKDIR $WORKDIR
ENTRYPOINT [ "bundle", "exec", "rspec" ]