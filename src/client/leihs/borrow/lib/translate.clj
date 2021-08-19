(ns leihs.borrow.lib.translate)

(defmacro set-default-translate-path [p]
  `(def ~'default-translate-path ~p))

(defmacro with-translate-path [p & body]
  `(binding [*default-path* ~p]
     ~@body))

(defmacro t
  ([dict-path] `(t ~dict-path nil))
  ([dict-path values]
   `(if *default-path*
      (t-base ~dict-path ~values)
      (do (declare ~'default-translate-path)
          (binding [*default-path* ~'default-translate-path]
            (t-base ~dict-path ~values))))))
