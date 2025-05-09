(ns leihs.borrow.reload
  (:require [clj-reload.core]))

(clj-reload.core/init {:dirs ["resources"
                              "shared-clj/resources"
                              "shared-clj/src"
                              "src/server"
                              "src/common"]
                       :no-unload '#{leihs.borrow.main}})
