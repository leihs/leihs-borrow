(load-file "shared-clj/deps.clj")

(set-env!
  :source-paths #{"src/all" "shared-clj/src"}
  :resource-paths #{"resources/all"}
  :project 'leihs-borrow
  :version "0.1.0-SNAPSHOT"
  :dependencies
  (extend-shared-deps
    '[[io.dropwizard.metrics/metrics-core "3.1.2"] ; lacinia does not work with the version from shared-deps
      [clj-http "3.10.0"]
      [clojure.java-time "0.3.2"]
      [com.layerware/hugsql "0.5.1"]
      [org.threeten/threeten-extra "1.2"]
      [threatgrid/ring-graphql-ui "0.1.1"]
      [org.clojure/spec.alpha "0.2.176"]]))

(task-options!
  target {:dir #{"target"}}
  aot {:all true}
  repl {:init-ns 'app}
  jar {:file "leihs-borrow.jar", :main 'leihs.borrow.main})

(deftask prod
  "Production profile to be used in combination with other tasks."
  []
  (with-pass-thru _
    (set-env! :resource-paths #(conj % "resources/prod"))))

(deftask uberjar
  "Build an uberjar of the application."
  []
  (comp (prod) ; does not seem to work, that's why sift later on
        (javac :options ["-release" "1.8" "-target" "1.8" "-source" "1.8" "-Xlint:-options"])
        (aot)
        (uber)
        (sift :add-resource #{"resources/all" "resources/prod"})
        (jar)
        (target)))

(deftask run
  "Run the application with given opts."
  []
  (comp 
    (with-pass-thru _
      (require 'leihs.borrow.main)
      (->> *args*
           (cons "run")
           (apply (resolve 'leihs.borrow.main/-main))) )
    (wait)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; DEV ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftask dev
  "Development profile to be used in combination with other tasks."
  []
  (with-pass-thru _
    (set-env! :source-paths #(conj % "src/dev")
              :resource-paths #(conj % "resources/dev"))
    (require 'app '[clojure.tools.namespace.repl :as ctnr])
    (apply (resolve 'ctnr/set-refresh-dirs) (get-env :directories))))

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
    (with-bindings {#'*ns* *ns*}
      ((resolve 'app/reset)))))

(deftask focus
  "Watch for changed files, reload namespaces and reset application state."
  []
  (comp (dev)
        (boot.task.built-in/repl "-s")
        (watch)
        (reset)))
