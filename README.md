# leihs-borrow

## Stack

### App (frontend)

- [Shadow CLJS](https://github.com/thheller/shadow-cljs)
- [Reagent](https://github.com/reagent-project/reagent)
- [Re-frame](https://github.com/day8/re-frame)

### UI component library and theme (`borrow-ui`)

See: [ui/README.md](ui/README.md)

### Translations

- translations strings are stored [in this repo in an EDN map](src/common/leihs/borrow/translations.cljc)
- we use the ICU syntax, lib: [`intl-messageformat`](https://formatjs.io/docs/intl-messageformat/)

## DEV

For seamless dependency installation: use [**asdf** version manager](https://asdf-vm.com)

### Quickstart (shell):

```shell
# ENV config: copy from template
cp .env.local-example .env.local.dev
cp .env.local-example .env.local.test
ln -sf .env.local.dev .env.local
# ln -sf .env.local.test .env.local # to switch settings to dev env

# == prepare ==

# ensure correct version of shared code (UI/React and ClojureScript) and DB migrations
git submodule update --init --recursive --force

# prepare UI (npm install and build)
bin/ui-build

# prepare DB
source bin/set-env && bin/db-migrate

# == run the services (in separate shells) ==

# run backend
source bin/set-env && bin/dev-run-backend

# run frontend
source bin/set-env && bin/dev-run-frontend

# run ui watch (only when working on ui components and theme)
cd ui && npm run watch

# run storybook (only when working on ui components and theme)
cd ui && npm run storybook

# open in browser:
open "http://localhost:3250/borrow/"  # borrow
open "http://localhost:6006/"         # storybook
```

### Quickstart (VS Code Task Runner)

- Make sure `.env.local.dev` is configured for your development DB
- Cmd-Shift-R and type "Run Task"
- Task "Development Preparations Steps"
- Task "Frontend Development Services (including UI)"

See `.vscode/tasks.json` for infos and more options

### Graph*i*QL (GraphQL console)

There is a built-in [console for the GraphQL API](https://github.com/graphql/graphiql/blob/main/packages/graphiql/README.md), but it needs some manual configuration to work.
It is enabled in all environments (so it can also be used on a server, not just the local development env).

- sign in
- open <http://localhost:3250/borrow/graphiql/> or <https://test.leihs.zhdk.ch/borrow/graphiql/> or similar
- configure
  - click button "Edit HTTP Headers"
  - add header with key `x-csrf-token`
  - the correct value is found in the `leihs-anti-csrf-token` cookie (try `javascript:alert(document.cookie)`)
- click the button "Fetch"
- if it worked, the output should show `Schema fetched`

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

## TEST

Start in prod mode:

```bash
bin/run
```

...or start in dev mode (instructions see above), but you might want to comment-out `:preloads [day8.re-frame-10x.preload]` in `shadow-cljs.edn` to prevent the `re-frame-10x` debugger from covering buttons etc.

Then run a spec:

```bash
./bin/rspec spec/features/smoke.feature
```

## PROD

compile it:

```shell
bin/build
```

start it:

```shell
# TODO: document
```

## Known issues

### app framework

- i18n: formatting numbers in translated messages uses browser locale instead of message locale.

### Formatting Code

#### Clojure

Use `./bin/cljfmt check` and `./bin/cljfmt fix`.

From vim you can use `:! ./bin/cljfmt fix %` to format the current file.

### Formatting Code

#### Ruby

Use `./bin/rblint` and `./bin/rblint --fix`.
