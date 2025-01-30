(ns leihs.borrow.lib.helpers
  (:require-macros leihs.borrow.lib.helpers)
  (:require [clojure.walk :refer [postwalk walk] :as walk]
            [clojure.string :as string]
            [camel-snake-kebab.core :as csk]
            ["date-fns" :as datefn]
            [goog.string :as gstring]
            [goog.string.format]))

(def format gstring/format)

(def keywordize-keys walk/keywordize-keys)

(defn- tranform-map-keys [transformer map]
  (postwalk #(cond-> %
               (and (keyword? %)
                    (not (qualified-keyword? %)))
               transformer)
            map))

(defn camel-case-keys [m]
  (tranform-map-keys csk/->camelCase m))

(defn kebab-case-keys [m]
  (tranform-map-keys csk/->kebab-case m))

(defn date-format-day [date]
  (datefn/format date "yyyy-MM-dd"))

(defn format-date-range [d1 d2 date-locale]
  (let [locale #js {:locale date-locale}
        d1-formatted (datefn/format d1 "P" locale)
        d2-formatted (datefn/format d2 "P" locale)]
    (cond (datefn/isSameDay d1 d2)
          d1-formatted
          (datefn/isSameYear d1 d2)
          (str (string/replace (datefn/format d1 "P" locale) (re-pattern (str "/?" (datefn/getYear d1))) "") " – " d2-formatted)
          :else (str d1-formatted " – " d2-formatted))))

(def log js/console.log)

(defn pp-js [x]
  (js/JSON.stringify x 0 2))

(defn pp [x] (-> x clj->js pp-js))

(defn obj->map [x]
  (-> x
      (js->clj :keywordize-keys true)
      kebab-case-keys))

(defn body-encode [data]
  (->> data
       (map (fn [[k v]]
              (vector (name k) "=" (str v))))
       (interpose "&")
       flatten
       string/join))
