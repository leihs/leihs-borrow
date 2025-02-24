(ns leihs.borrow.resources.inventory-pools.email-variables
  (:require
   [clojure.string :as string]
   [leihs.borrow.resources.holidays :as holidays]
   [leihs.borrow.resources.workdays :as workdays]
   [leihs.borrow.translate :refer [t]]
   [leihs.borrow.translate-base :refer [fallbacks]]
   [java-time.api :as jt]
   [taoensso.timbre :as timbre :refer [debug spy]]))

(def dict-path :borrow.terms.weekdays)

(def formats {:de-CH {:default "dd.MM.YYYY"
                      :short "dd.MM"}
              :en-GB {:default "dd/MM/YYYY"
                      :short "dd/MM"}})

(defn fallbacked [tx locale]
  (if (contains? formats locale)
    locale
    (get fallbacks locale)))

(defn merge-workdays [tx locale pool]
  (let [workdays (workdays/get-by-pool-id tx (:id pool))]
    (->> workdays/DAYS
         (reduce (fn [memo day]
                   (let [day* (-> day name string/lower-case)
                         day-info (if ((keyword day*) workdays)
                                    ((keyword (str day* "_info")) workdays)
                                    (string/lower-case (t :borrow.pool-show.closed locale)))
                         line (str (-> day*
                                       (->> (str dict-path "."))
                                       (t locale))
                                   ": "
                                   day-info)]
                     (conj memo line)))
                 [])
         (string/join "\n")
         (assoc pool :workdays))))

(defn merge-holidays [tx locale pool]
  (let [holidays (holidays/get-by-pool-id tx (:id pool))
        locale* (fallbacked tx locale)]
    (->> holidays
         (map (fn [{:keys [start_date end_date name]}]
                (let [start-date (jt/local-date start_date)
                      end-date (jt/local-date end_date)
                      span (if (= start-date end-date)
                             (jt/format (-> formats locale* :default) start-date)
                             (let [sd-format (if (= (jt/year start-date) (jt/year end-date))
                                               :short
                                               :default)]
                               (str (jt/format (-> formats locale* sd-format) start-date)
                                    "\u2013" ; en-dash
                                    (jt/format (-> formats locale* :default) end-date))))]
                  (str name ": " span))))
         (string/join "\n")
         (assoc pool :holidays))))

(comment
  (require '[leihs.core.db :as db])
  (jt/format "dd/MM" (jt/local-date "2019-12-25"))
  (let [tx (db/get-ds)
        locale :en-US]
    #_(merge-workdays tx locale {:id #uuid "8bd16d45-056d-5590-bc7f-12849f034351"})
    (merge-holidays tx locale {:id #uuid "8bd16d45-056d-5590-bc7f-12849f034351"})))
