(ns radio-test.events
  (:require
   [cljs.pprint :refer [cl-format]]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [radio-test.db :as db]
   [radio-test.sci :refer [init-context parent-id]]
   [re-frame.core :as re-frame]
   [sci.core :as sci]
   [clojure.edn :as edn]
   [radio-test.config :as config]))

;; INIT DB
(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
            db/default-db))

(re-frame/reg-event-db
 ::set-re-pressed-example
 (fn [db [_ value]]
   (assoc db :re-pressed-example value)))

;; -------------------------
;; STATION INTERNAL EVENTS
;; -------------------------

(re-frame/reg-event-db
 ::update-input-text
 (fn [db [_ key value]]
   (assoc-in db [:sci :stations key :input-text] value)))

(re-frame/reg-event-fx
 ::log
 (fn [_ [_ value]]
   (.log js/console value)))

(re-frame/reg-event-db
 ::reset-context
 (fn [db [_ ctx]]
   (let [_  (tap> ctx)
         ctx (sci/eval-string ctx)]
     (assoc-in db [:sci :ctx] (merge (init-context) ctx)))))

(add-tap #(.log js/console %))

(re-frame/reg-event-db
 ::eval-sci
 (fn [db [_ key input]]
   (let [ctx (get-in db [:sci :ctx])
         result (sci/binding [parent-id (str "something" "." (key->js key))]
                  (sci/eval-string* ctx input))]
     (assoc-in db [:sci :stations key :eval-result] result))))

(re-frame/reg-event-db
 ::eval-all
 (fn [db [_]]
   (let [source (->> (get-in db [:sci :stations])
                     (vals)
                     (map #(:input-text %))
                     (reduce str))
         _ (tap> (->> (.getSelection js/window)
                      (.toString)))
         ctx (get-in db [:sci :ctx])
         result (sci/eval-string* ctx source)]
     (assoc-in db [:sci :global :eval-result] result))))

;; Storing Values
(re-frame/reg-event-db
 ::set-var
 (fn [db [_ key value]]
   (if (coll? key)
     (assoc-in db (vec (concat [:sci :vars] key)) value)
     (assoc-in db [:sci :vars key] value))))
