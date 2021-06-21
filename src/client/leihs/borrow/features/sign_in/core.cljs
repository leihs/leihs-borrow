(ns leihs.borrow.features.sign-in.core
  (:require 
    [ajax.core :as ajax]
    [clojure.string :as string]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [leihs.borrow.lib.helpers :refer [log spy]]
    [leihs.borrow.csrf :as csrf]
    [leihs.borrow.lib.re-frame :refer [reg-event-fx
                                       reg-event-db
                                       reg-sub
                                       reg-fx
                                       subscribe
                                       dispatch
                                       dispatch-sync]]
    [leihs.core.constants :as constants]
    [leihs.core.url.shared :refer [encode]]
    [reagent.core :as reagent]))

(reg-event-db ::on-success
              (fn-traced [db _]
                (update-in db
                           [:meta :app :fatal-errors]
                           (partial filter #(-> % :extensions :code (not= 401))))))

(reg-event-db ::on-failure
              (fn-traced [db _] (log "failure") db))

(defn body-encode [data]
  (->> data
       (map (fn [[k v]]
              (vector (name k) "=" (str v))))
       (interpose "&")
       flatten
       string/join))

(reg-event-fx
  ::submit
  (fn-traced [_ [_ data]]
    {:http-xhrio {:method :post
                  :uri "/sign-in"
                  :body (body-encode data)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [::on-success]
                  :on-failure [::on-failure]}}))

(defn login-error-screen []
  (let [user (reagent/atom nil)
        password (reagent/atom nil)]
    (fn []
      [:section.p-4
       {:style {:white-space "pre-wrap" :background "salmon" :padding "1rem"}}
       [:h1 "You got logged out."]
       ; [:p [:button.border-black.border-2.rounded-full.py-1.px-3
       ;        {:type :button,
       ;         :on-click #(-> js/window (.-location) (.reload))} "CLICK HERE TO LOGIN!"]]
       [:form {:class "ui-form-signin mt-4"}
        [csrf/token-field]
        [:label {:for "inputEmail", :class "sr-only"} "Login or email"]
        [:input {:id "inputEmail",
                 :name "user",
                 :class "form-control",
                 :value @user
                 :on-change (fn [e] (reset! user (-> e .-target .-value)))
                 :placeholder "Login or email"
                 :required true ,
                 :style {:margin-bottom "-1px", :border-bottom-right-radius 0, :border-bottom-left-radius 0}}]
        [:label {:for "inputPassword", :class "sr-only"} "Password"]
        [:input {:type "password",
                 :name "password",
                 :id "inputPassword",
                 :value @password
                 :on-change (fn [e] (reset! password (-> e .-target .-value)))
                 :placeholder "Password",
                 :required true,
                 :class "form-control",
                 :style {:border-top-right-radius 0, :border-top-left-radius 0}}]
        [:button {:class "btn btn-success btn-block mt-3",
                  :type "submit"
                  :on-click (fn [e]
                              (.preventDefault e)
                              (dispatch [::submit {:user @user
                                                   :password @password
                                                   constants/ANTI_CSRF_TOKEN_FORM_PARAM_NAME csrf/token}]))}
         "Submit"]]])))
