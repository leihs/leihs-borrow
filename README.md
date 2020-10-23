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
