; macros part of the same-named-namespace
; see: https://clojurescript.org/guides/ns-forms#_implicit_sugar

(ns leihs.borrow.lib.helpers)

(defmacro spy [expr]
  `(let [res# ~expr]
     (js/console.log res#)
     res#))
