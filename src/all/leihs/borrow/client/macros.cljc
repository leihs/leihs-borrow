(ns leihs.borrow.client.macros)

(defmacro spy [expr]
  `(let [res# ~expr]
     (js/console.log res#)
     res#))
