(ns radio-test.subs
  (:require
   [cljs.pprint :refer [cl-format]]
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::visible-frames
 (fn [db [_]]
   (let [{:keys [visible-fn render-fn]} (get-in db [:sci :vars])]
     (->> (:frames db)
          (vals)
          (filter (or visible-fn #(:visible? %)))
          (mapv (fn [v] [(:component v)]))))))

(re-frame/reg-sub
 ::frame-data
 (fn [db [_ id]]
   (get-in db [:frames id])))

(re-frame/reg-sub
 ::frame-pos
 (fn [db [_ id]]
   (or (get-in db [:frames id :pos])
       {:x 0
        :y 0})))

(re-frame/reg-sub
 ::re-pressed-example
 (fn [db _]
   (:re-pressed-example db)))

(re-frame/reg-sub
 ::sci-values
 (fn [db [_ ns-string]]
   (or (get-in db [:namespaces (keyword ns-string)])
       {:input-text (cl-format nil "(ns ~a\n  (:require [user :refer :all]))" ns-string)
        :eval-result nil})))

(re-frame/reg-sub
 ::sci-context
 (fn [db [_]]
   (get-in db [:sci :ctx])))

(re-frame/reg-sub
 ::sci-namespaces
 (fn [{:keys [namespaces]} [_]]
   (->> namespaces
        keys
        (map key->js))))

(re-frame/reg-sub
 ::station-keys
 (fn [db [_]]
   (let [project-name (keyword (:project-name db))]
     (keys (get-in db [:namespaces project-name])))))

;; Fetch a global sci var
(re-frame/reg-sub
 ::get-var
 (fn [db [_ keys]]
   (get-in db (vec (concat [:sci :vars] keys)))))

(re-frame/reg-sub
 ::get-editor-state
 (fn [db [_ ns-string]]
   (get-in db [:editors (keyword ns-string)])))
