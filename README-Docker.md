# docker support

## plan

start with running borrow app tests with github actions

- [x] build image for testing, based on pre-built app
  - [x] clojure
  - [x] ruby/rails for legacy-api
    - [] fix rails autoloading problem
- [ ] build app in container

  - [ ] leihs ui
  - [ ] uberjar

- [ ] support development inside container

- multi stage ?
  - base image with ruby, bundler, etc.
    - only things that should never break locally
    - no bundle install etc
  - prod image with installed deps and all needed code/jars
  - dev image, removed code/compilations
    - should not inherit from prod (saves time?)

## questions

- ruby 2.6 or 2.7.5? `cider-ci/*` and `.ruby-version` differ. what is correct, what do we want
  - in any case we should use `x.y`, without patch version, because we want to automatically upgrade?

---

## Docker usage

### docker compose

```bash
alias dc="docker compose"
```

start up everything:

```bash
dc up --build # -d flag to not run in the foreground
```

start it up and run tests:

```bash
dc build shared-ui
dc build
dc up -d db firefox borrow
dc restart firefox
dc run rspec spec/features/smoke.feature
```

while its running:

```bash
# open VNC viewer to look at the selenium firefox
open 'http://localhost:7901/?autoconnect=1&reconnect=1&resize=scale'

# console inside the already running container
dc exec legacy bash

# re-build (if Dockerfile or deps changed)
dc build legacy

# restart a service
dc restart legacy

# restart all
dc restart

# stream all the logs (usefull if sent to background with `up -d`)
dc logs -f

# rails console
dc exec legacy bundle exec rails c
```

#### interactive console

Processes can be run interactive (for legacy/rails, `binding.pry` works).
If started this way,other commands need to be run with `--no-deps` or it will try to start twice (and fail).

```bash
dc up -d
dc stop legacy
dc run --rm --service-ports legacy
dc run --rm --no-deps rspec rspec spec/features/smoke.feature
```

### running the service manually

```bash
export LEIHS_SECRET=secret
export PGDATABASE="leihs"
export PGUSER="$(whoami)"
export PGPASSWORD="leihs"
export PGHOST="host.docker.internal"
export DATABASE_URL="postgresql://${PGUSER}:${PGPASSWORD}@${PGHOST}:5432/${PGDATABASE}?max-pool-size=5"

docker build -t leihs-legacy -f Dockerfile .
# run container with optional command, if not given the server is started
docker run --rm -it \
    -v ${PWD}/../.git:/leihs/.git \
    -v ${PWD}:/leihs/legacy \
    -p 3000:3000 \
    -e LEIHS_SECRET -e DATABASE_URL -e PGDATABASE -e PGHOST -e PGUSER -e PGPASSWORD \
    leihs-legacy # optional: bin/db-create
```

### debugging

```bash
# run a fresh container to try out something. will self-delete on exit.
docker run --rm -it ruby:2.7.5-bullseye /bin/bash
# or with the leihs app inside:
docker run --rm -it -v ${PWD}:/leihs/legacy leihs-legacy /bin/bash
```

or when using the `docker-compose.yml` in the superproject:

```bash
docker-compose run legacy /bin/bash
```
