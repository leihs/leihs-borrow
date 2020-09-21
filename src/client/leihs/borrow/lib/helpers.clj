; macros part of the same-named-namespace
; see: https://clojurescript.org/guides/ns-forms#_implicit_sugar

(ns leihs.borrow.lib.helpers
  (:refer-clojure :exclude [ns-name]))

(defmacro spy [expr]
  `(let [res# ~expr]
     (js/console.log res#)
     res#))

(defmacro ns-name []
  `(quote ~(:name (:ns &env))))
