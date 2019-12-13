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
1. `shadow-cljs watch app`
2. Open your app in the browser.
3. Open vim. In case you are already using vim-fireplace for clj code (lein or boot), then in a new terminal. Otherwise vim-fireplace will get confused with the nrepl-port.
4. In vim `:Connect <nrepl-port>`
5. In vim `:Piggieback :app`

More info: https://github.com/tpope/vim-fireplace/issues/322

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
