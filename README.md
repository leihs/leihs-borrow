# leihs-borrow

## DEV

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

## TEST

### prepare

```bash
git submodule update --init --recursive
bundle install
source ./scripts/_test-env.sh
cd database && bundle install && { bundle exec rake db:environment:set db:drop || true ;} && bundle exec rake db:create db:migrate && cd -
```

### run

```bash
# 1. start server (or build an uberjar and start this, see [#PROD](#prod))
./scripts/start-backend-test
# 2.
./scripts/start-legacy-test
# 3.
source ./scripts/_test-env.sh
bundle exec rspec spec/graphql/models/availability_spec.rb
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

## UI / Styleguide

### dev

```bash
cd leihs-ui
npm i
npm run build:theme
npm run storybook
```

### update API data examples

same setup as in [TEST](#test)!

```bash
rm -rf tmp
bundle exec rspec "spec/graphql/models/availability_spec.rb"
mv "tmp/spec_artefacts/spec/graphql" "leihs-ui/static/api-examples"
```
