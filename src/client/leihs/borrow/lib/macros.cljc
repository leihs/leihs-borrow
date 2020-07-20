(ns leihs.borrow.lib.macros)

(defmacro spy [expr]
  `(let [res# ~expr]
     (js/console.log res#)
     res#))