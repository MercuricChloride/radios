(ns radio-test.subs
  (:require
   [re-frame.core :as re-frame]
   [sci.core :as sci]
   [cljs.pprint :refer [cl-format]]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::project-name
 (fn [db]
   (:project-name db)))

(re-frame/reg-sub
 ::re-pressed-example
 (fn [db _]
   (:re-pressed-example db)))

(re-frame/reg-sub
 ::sci-values
 (fn [db [_ project-name key]]
   (or (get-in db [:namespaces (keyword project-name) key])
       {:input-text (cl-format nil "(ns ~a.~a)" project-name (key->js key))
        :eval-result nil})))

(re-frame/reg-sub
 ::sci-context
 (fn [db [_]]
   (get-in db [:sci :ctx])))

(re-frame/reg-sub
 ::sci-namespaces
 :<- [::sci-context]
 (fn [[ctx] [_]]
   (sci/all-ns ctx)))

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
