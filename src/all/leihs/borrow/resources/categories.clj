(ns leihs.borrow.resources.categories
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.executor :as executor]
            [leihs.core.sql :as sql]
            [leihs.borrow.paths :refer [path]]
            [leihs.borrow.resources.images :as images]
            [leihs.borrow.resources.models :as models]))

(def base-sqlmap
  (-> (sql/select :model_groups.id [:model_groups.name :name])
      (sql/modifiers :distinct)
      (sql/from :model_groups)
      (sql/merge-where [:= :model_groups.type "Category"])))

(defn extend-based-on-args [sqlmap {:keys [limit offset ids root-only]}]
  (-> sqlmap
      (cond-> root-only
        (sql/merge-where
          [:not
           [:exists
            (-> (sql/select true)
                (sql/from :model_group_links)
                (sql/merge-where [:=
                                  :model_groups.id
                                  :model_group_links.child_id]))]]))
      (cond-> limit (sql/limit limit))
      (cond-> offset (sql/offset offset))
      (cond-> (seq ids)
        (sql/merge-where [:in :model_groups.id ids]))))

(defn get-multiple
  [{{:keys [tx authenticated-entity]} :request :as context} args value]
  (def ctx context)
  (log/debug (executor/selections-seq context))
  (-> base-sqlmap
      (sql/merge-join :model_links
                      [:= :model_groups.id :model_links.model_group_id])
      (sql/merge-join :models
                      [:= :model_links.model_id :models.id])
      (models/merge-reservable-conditions (:id authenticated-entity))
      (extend-based-on-args args)
      (cond-> value
        (#(case (::lacinia/container-type-name context)
            :Category (cond
                        (executor/selects-field? context :Category/children)
                        (-> %
                            (sql/select :model_groups.id
                                        [(sql/call :coalesce
                                                   :model_group_links.label
                                                   :model_groups.name) :name])
                            (sql/merge-join :model_group_links
                                            [:=
                                             :model_groups.id
                                             :model_group_links.child_id])
                            (sql/merge-where [:=
                                              :model_group_links.parent_id
                                              (:id value)]))
                        (:Category/parents) (-> %
                                                (sql/select :model_groups.id
                                                            [(sql/call :coalesce
                                                                       :model_group_links.label
                                                                       :model_groups.name) :name])
                                                (sql/merge-join :model_group_links
                                                                [:=
                                                                 :model_groups.id
                                                                 :model_group_links.parent_id])
                                                (sql/merge-where [:=
                                                                  :model_group_links.child_id
                                                                  (:id value)]))
                        %)
            :Model (sql/merge-where % [:= :models.id (:id value)]))))
      sql/format
      ; log/spy
      (->> (jdbc/query tx))))

(comment 
  (require '[com.walmartlabs.lacinia.executor :as executor])
  (executor/selections-seq ctx ))

; (comment 
;   {:com.walmartlabs.lacinia/enable-timing? true,
;    :com.walmartlabs.lacinia/container-type-name :Category,
;    :com.walmartlabs.lacinia/selection {:com.walmartlabs.lacinia.parser/arguments-extractor nil,
;                                        :selections [{:com.walmartlabs.lacinia.parser/arguments-extractor nil, :selections nil, :arguments nil, :reportable-arguments nil, :field :name, :selection-type :field, :alias :name, :leaf? true, :com.walmartlabs.lacinia.parser/prepare-directives? true, :concrete-type? true, :com.walmartlabs.lacinia.parser/needs-prepare? true, :field-definition {:type {:kind :non-null, :type {:kind :root, :type :String}}, :description "A name is either a label for the child-parent connection (if such exists) or the name of the category itself.", :field-name :name, :qualified-name :Category/name, :args {}, :type-name :Category, :resolve #object[com.walmartlabs.lacinia.schema$default_field_resolver$default_resolver__32279 0x3aa88cb0 "com.walmartlabs.lacinia.schema$default_field_resolver$default_resolver__32279@3aa88cb0"], :selector #object[com.walmartlabs.lacinia.schema$assemble_selector$select_non_null__31845 0x1ccb90df "com.walmartlabs.lacinia.schema$assemble_selector$select_non_null__31845@1ccb90df"], :direct-fn :name}, :location {:line 22, :column 7}, :directives []}],
;                                        :arguments nil,
;                                        :com.walmartlabs.lacinia.parser/prepare-nested-selections? true,
;                                        :reportable-arguments nil,
;                                        :field :parents,
;                                        :selection-type :field,
;                                        :alias :parents,
;                                        :leaf? false,
;                                        :com.walmartlabs.lacinia.parser/prepare-directives? true,
;                                        :concrete-type? true,
;                                        :com.walmartlabs.lacinia.parser/needs-prepare? true,
;                                        :field-definition {:type {:kind :list, :type {:kind :root, :type :Category}}, :resolve #object[com.walmartlabs.lacinia.schema$wrap_resolver_to_ensure_resolver_result$fn__31806 0x108e8367 "com.walmartlabs.lacinia.schema$wrap_resolver_to_ensure_resolver_result$fn__31806@108e8367"], :field-name :parents, :qualified-name :Category/parents, :args {}, :type-name :Category, :description nil, :selector #object[com.walmartlabs.lacinia.schema$assemble_selector$select_list__31849 0x5003a4c1 "com.walmartlabs.lacinia.schema$assemble_selector$select_list__31849@5003a4c1"], :direct-fn nil},
;                                        :location {:line 21, :column 5},
;                                        :directives []}
;    })

;#### debug ###################################################################
; (logging-config/set-logger! :level :debug)
; (logging-config/set-logger! :level :info)
; (debug/debug-ns 'cider-ci.utils.shutdown)
; (debug/debug-ns *ns*)
; (debug/undebug-ns *ns*)
