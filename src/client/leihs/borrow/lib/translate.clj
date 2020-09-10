(ns leihs.borrow.lib.translate)

(defmacro set-default-translate-path [p]
  `(def ~'default-translate-path ~p))

(defmacro t [k]
  `(do (declare ~'default-translate-path)
       (binding [*default-path* ~'default-translate-path]
         (t-base ~k))))
