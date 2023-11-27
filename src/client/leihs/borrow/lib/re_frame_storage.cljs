; Copied from https://github.com/akiroz/re-frame-storage/blob/master/src/akiroz/re_frame/storage.cljs
; Added support for session-storage.

(ns leihs.borrow.lib.re-frame-storage
  (:require [re-frame.core :refer [reg-fx reg-cofx ->interceptor]]
            [alandipert.storage-atom :refer [local-storage session-storage]]
            [leihs.borrow.lib.helpers :as h]
            [cljs.spec.alpha :as s]))

(s/def ::cljs-data
  (s/or :nil      nil?
        :boolean  boolean?
        :number   number?
        :string   string?
        :keyword  keyword?
        :symbol   symbol?
        :uuid     uuid?
        :date     (partial instance? js/Date)
        :list     (s/coll-of  ::cljs-data :kind list?)
        :vector   (s/coll-of  ::cljs-data :kind vector?)
        :set      (s/coll-of  ::cljs-data :kind set?)
        :map      (s/map-of   ::cljs-data ::cljs-data)))

;; atom containing local-storage atoms
(def storage-atoms (atom {}))

(defn register-store [store-key session?]
  (when-not (@storage-atoms store-key)
    (swap! storage-atoms assoc store-key
           ((if session? session-storage local-storage) (atom nil) store-key))))

(s/fdef register-store
  :args (s/cat :store-key keyword?
               :session? boolean?))

(defn ->store [store-key data]
  (reset! (@storage-atoms store-key) data))

(s/fdef ->store
  :args (s/cat :store-key keyword?
               :data ::cljs-data))

(defn <-store [store-key]
  @(@storage-atoms store-key))

(s/fdef <-store
  :args (s/cat :store-key keyword?)
  :ret  ::cljs-data)

(defn reg-co-fx! [store-key {:keys [fx cofx]} session?]
  (register-store store-key session?)
  (when fx
    (reg-fx
     fx
     (fn [data]
       (->store store-key data))))
  (when cofx
    (reg-cofx
     cofx
     (fn [coeffects _]
       (assoc coeffects cofx (<-store store-key))))))

(s/def ::fx keyword?)
(s/def ::cofx keyword?)
(s/fdef reg-co-fx!
  :args (s/cat :store-key keyword?
               :handlers (s/keys :req-un [(or ::fx ::cofx)])
               :session? boolean?))

(defn persist-db [store-key db-key session?]
  (register-store store-key session?)
  (->interceptor
   :id (keyword (str db-key "->" store-key))
   :before (fn [context]
             (assoc-in context [:coeffects :db db-key]
                       (<-store store-key)))
   :after (fn [context]
            (when-let [value (get-in context [:effects :db db-key])]
              (->store store-key value))
            context)))

(s/fdef persist-db
  :args (s/cat :store-key keyword?
               :db-key keyword?
               :session boolean?))

(defn persist-db-keys [store-key db-keys session?]
  (register-store store-key session?)
  (->interceptor
   :id (keyword (str (apply str (sort db-keys)) "->" store-key))
   :before (fn [context]
             (update-in context [:coeffects :db] merge (<-store store-key)))
   :after (fn [context]
            (when-let [value (some-> (get-in context [:effects :db])
                                     (select-keys db-keys))]
              (->store store-key value))
            context)))

(s/fdef persist-db-keys
  :args (s/cat :store-key keyword?
               :db-keys (s/coll-of keyword?)
               :session? boolean?))