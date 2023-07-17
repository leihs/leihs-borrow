(ns leihs.borrow.lib.form-helpers
  (:require [reagent.core :as reagent]
            [reagent.impl.template :as rtpl]
            [react :as react]
            ["/borrow-ui" :as UI]))

;; NOTE: workaround for reagent input handling when using custom components
;;       for details see <https://github.com/reagent-project/reagent/blob/b71fc361b85338ef4e4cd52a7b21e0f3f3f89628/doc/ControlledInputs.md>
;;       we implement same solutions as MUI: <https://github.com/reagent-project/reagent/blob/master/doc/examples/material-ui.md>
;;       https://github.com/reagent-project/reagent/blob/396b375d2d6686cc104d3d99e291ba6a32ade54a/examples/react-mde/src/example/core.cljs

(def textarea-component
  (react/forwardRef
   (fn textarea [props ref]
     (let [props (assoc (js->clj props) :ref ref)]
       (reagent/as-element [:textarea props])))))

(defn UiTextarea [props & children]
  (let [props (-> props
                  (assoc :inputComponent textarea-component)
                  rtpl/convert-prop-value)]
    (apply reagent/create-element
           UI/Components.Design.Textarea
           props
           (map reagent/as-element children))))

(def input-component
  (react/forwardRef
   (fn textarea [props ref]
     (let [props (assoc (js->clj props) :ref ref)]
       (reagent/as-element [:input props])))))

(defn UiInputWithClearButton [props & children]
  (let [props (-> props
                  (assoc :inputComponent input-component)
                  rtpl/convert-prop-value)]
    (apply reagent/create-element
           UI/Components.Design.InputWithClearButton
           props
           (map reagent/as-element children))))

(defn UiDatepicker [props & children]
  (let [props (-> props
                  (assoc :inputComponent input-component)
                  rtpl/convert-prop-value)]
    (apply reagent/create-element
           UI/Components.Design.DatePicker
           props
           (map reagent/as-element children))))

(defn UiDateRangePicker [props & children]
  (let [props (-> props
                  (assoc :inputComponent input-component)
                  rtpl/convert-prop-value)]
    (apply reagent/create-element
           UI/Components.Design.DateRangePicker
           props
           (map reagent/as-element children))))
