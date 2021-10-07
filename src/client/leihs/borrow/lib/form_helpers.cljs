(ns leihs.borrow.lib.form-helpers
  (:require [reagent.core :as reagent]
            [reagent.impl.template :as rtpl]
            ["/leihs-ui-client-side-external-react" :as UI]))

;; NOTE: workaround for reagent input handling when using custom components
;;       for details see <https://github.com/reagent-project/reagent/blob/b71fc361b85338ef4e4cd52a7b21e0f3f3f89628/doc/ControlledInputs.md>
;;       we implement same solutions as MUI: <https://github.com/reagent-project/reagent/blob/master/doc/examples/material-ui.md>
;; TODO: should be moved into some combined "UI" namespace so its more easy to use the correct version!
(def textarea-component
  (reagent/reactify-component (fn [props] [:textarea props])))

(defn UiTextarea [props & children]
  (let [props (-> props
                  (assoc :inputComponent textarea-component)
                  rtpl/convert-prop-value)]
    (apply reagent/create-element
           UI/Components.Design.Textarea
           props
           (map reagent/as-element children))))
;; END OF workaround for reagent input handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def input-component
  (reagent/reactify-component (fn [props] [:input props])))

(defn UiInputWithClearButton [props & children]
  (let [props (-> props
                  (assoc :inputComponent input-component)
                  rtpl/convert-prop-value)]
    (apply reagent/create-element
           UI/Components.Design.InputWithClearButton
           props
           (map reagent/as-element children))))
