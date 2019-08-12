(load-file "shared-clj/deps.clj")

(set-env!
  :source-paths #{"src/all" "shared-clj/src"}
  :resource-paths #{"resources"}
  :project 'leihs-borrow
  :version "0.1.0-SNAPSHOT"
  :dependencies (extend-shared-deps '[[clj-pid "0.1.2"]
                                      [spootnik/signal "0.2.1"]]))

(task-options!
  target {:dir #{"target"}}
  aot {:all true}
  repl {:init-ns 'user}
  sift {:include #{#"leihs-borrow.jar"}}
  jar {:file "leihs-borrow.jar", :main 'leihs.borrow.main})

(deftask uberjar
  "Build an uberjar of the application."
  []
  (comp (aot)
        (uber)
        (jar)
        (sift)
        (target)))

(deftask run
  "Run the application with given opts."
  []
  (require 'leihs.borrow.main)
  (->> *args*
       (cons "run")
       (apply (resolve 'leihs.borrow.main/-main)))
  (wait))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; DEV ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftask dev
  "Development profile to be used in combination with other tasks."
  []
  (set-env! :source-paths #(conj % "src/dev"))
  (require 'reset '[clojure.tools.namespace.repl :as ctnr])
  identity)

(ns-unmap *ns* 'repl)
(deftask repl
  "Overriding built-in repl with dev profile."
  []
  (comp (dev)
        (boot.task.built-in/repl)))

(deftask reset
  "Reset changed namespaces using clojure.tools.namespace."
  []
  ; use `resolve` because of dynamic `require` (not top-level):
  ; https://github.com/boot-clj/boot/wiki/Boot-Troubleshooting#why-isnt-require-working-in-my-pod
  (with-pass-thru _
    (apply (resolve 'ctnr/set-refresh-dirs)
           (get-env :directories))
    (with-bindings {#'*ns* *ns*}
      ((resolve 'reset/reset)))))

(deftask
  focus
  "Watch for changed files, reload namespaces and reset application state."
  []
  (comp (dev)
        (boot.task.built-in/repl "-s")
        (watch)
        (reset)))
