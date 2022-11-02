# leihs-borrow

## stack

- (to be documented)

### translations

- translations strings are stored [in this repo in an EDN map](src/common/leihs/borrow/translations.cljc)
- for the translation function we currently use `tongue`
- long-term we maybe want to use a more generic translation function (for example [Format.JS](https://formatjs.io/docs/core-concepts/icu-syntax) or [i18next](https://www.i18next.com/translation-function/essentials))
- we definitly want to use the ICU syntax (could just reuse the underlying lib [`intl-messageformat`](https://formatjs.io/docs/intl-messageformat/))
- in the meantime, to make a later adaptation easier, we just a subset on the `tongue` API: only
  - `"plain strings"` and
  - simple interpolation `"hello {username}"`

## DEV

quickstart:

```shell
# ENV config: copy from template
cp .env.local-example .env.local.dev
cp .env.local-example .env.local.test
ln -sf .env.local.dev .env.local
# ln -sf .env.local.test .env.local # to switch settings to dev env

# preparation steps:
# ensure correct version of shared code (UI/React and ClojureScript) and DB migrations
git submodule update --init --recursive --force

# In VS Code, do "Run Task" > "App Development" or manually start the following:
# (when resuming work, preparation steps can be skipped by running the task "Frontend Development Services")
bin/ui-prepare
source bin/set-env && bin/db-migrate

# run in a shell:
source bin/set-env && bin/dev-run-backend

# run in another shell:
source bin/set-env && bin/dev-run-frontend

# run in another shell:
source bin/set-env && bin/dev-run-legacy

# run in another shell:
cd leihs-ui && npm run watch:lib

# run in another shell:
cd leihs-ui && npm run storybook

# open in browser:
open "http://localhost:3210/borrow"       # sign in / old borrow
open "http://localhost:3250/app/borrow/"  # new borrow
open "http://localhost:9009/"             # storybook
```

### Graph*i*QL (GraphQL console)

There is a built-in [console for the GraphQL API](https://github.com/graphql/graphiql/blob/main/packages/graphiql/README.md), but it needs some manual configuration to work.
It is enabled in all environments (so it can also be used on a server, not just the local development env).

- sign in
- open <http://localhost:3250/app/borrow/graphiql/> or <https://test.leihs.zhdk.ch/app/borrow/graphiql/> or similar
- configure
  - click button "Edit HTTP Headers"
  - add header with key `x-csrf-token`
  - the correct value is found in the `leihs-anti-csrf-token` cookie (try `javascript:alert(document.cookie)`)
- click the button "Fetch"
- if it worked, the output should show `Schema fetched`

### boot watcher

1. Setup your bash environment.
2. `$ boot focus`

### shadow-cljs watcher

`$ shadow-cljs watch <ID>`

### shadow-cljs repl

1. `$ shadow-cljs clj-repl`
2. `[1:0]~shadow.user=> (shadow/watch <ID>)`
3. Open the app in browser.
4. `[1:1]~cljs.user=> (shadow/repl <ID>)`
5. `[1:1]~cljs.user=> (js/console.log "hello")`

### vim-fireplace & shadow-cljs

https://github.com/nimaai/vim-shadow-cljs

## RUN TESTS

```bash
./scripts/prepare-shared-ui.sh \
&& ./scripts/build-uberjar-prod.sh \
&& ./scripts/start-backend-test
```

```bash
./scripts/start-legacy-test
```

```bash
while ! curl -I --silent --fail http://localhost:3250; do sleep 5; done \
&& be rspec spec/features/smoke.feature
```

## PROD

compile it:

```shell
# Build frontend
npx shadow-cljs release app

# Build backend
boot uberjar
```

start it:

```shell
java -jar ./target/leihs-borrow.jar run
```

### known issues

#### app framework

- i18n: formatting numbers in translated messages uses browser locale instead of message locale.

#### tech stack

- we need to install `tslib` in our `package.json`,so that `intl-messageformat` works correctly
  - see bug report https://github.com/formatjs/formatjs/issues/2645
  - we are using a newer version of the package which is fixed, but the `tslib` dep is not picked up. we dont know why, hence the workaround of installing it ourselves

#### developing and building on Apple Silicon

Some fixes needed for Apple Silicon and/or newer macOS versions.

```sh
# ruby 2.6.6
RUBY_CFLAGS=-DUSE_FFI_CLOSURE_ALLOC rbenv install 2.6.6

# JAVA 11
brew install java11
brew install clojure

# boot needs a not-yet-released fix
git clone https://github.com/eins78/boot && cd boot
echo 'version=2.8.4-fix.1' > version.properties
make deps && make install

export BOOT_VERSION=2.8.4-fix.1
bin/build
```
