(ns leihs.borrow.resources.entitlements
  (:require [hugsql.core :as hugsql]))

(hugsql/def-sqlvec-fns "sql/entitlements.sql")

(def all-sql
  (first (all-entitlements-sqlvec)))
