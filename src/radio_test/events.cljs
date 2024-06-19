(ns radio-test.events
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [radio-test.db :as db]
   [radio-test.sci :refer [init-context parent-id]]
   [re-frame.core :as re-frame :refer [inject-cofx]]
   [reagent.dom :as rdom]
   [sci.core :as sci]))

;; Stores an item in local storage
(re-frame/reg-fx
 :local-store
 (fn [[path value]]
   (.setItem js/localStorage (clj->js path) (clj->js value))))

;; Loads local-storage into the cofx map
(re-frame/reg-cofx
 :local-get
 (fn [cfx path]
   (assoc cfx :local-storage (.getItem js/localStorge (clj->js path)))))

;; This will create a new frame in the app
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
 ::load-input-text
 (fn [db [_ ns-string]]))

(re-frame/reg-event-db
 ::update-input-text
 (fn [db [_ ns-string value]]
   (assoc-in db [:namespaces (keyword ns-string) :input-text] value)))

(re-frame/reg-event-fx
 ::log
 (fn [_ [_ value]]
   (.log js/console value)))

(re-frame/reg-event-db
 ::reset-context
 (fn [db [_ ctx]]
   (let [_   (tap> ctx)
         ctx (sci/eval-string ctx)]
     (assoc-in db [:sci :ctx] (merge (init-context) ctx)))))

(re-frame/reg-event-db
 ::eval-sci
 (fn [db [_ ns-string input]]
   (let [ctx    (get-in db [:sci :ctx])
         result (sci/binding [parent-id ns-string]
                  (sci/eval-string* ctx input))]
     (assoc-in db [:namespaces (keyword ns-string) :eval-result] result))))

(re-frame/reg-event-db
 ::eval-all
 (fn [db [_]]
   (let [source (->> (get-in db [:sci :stations])
                     (vals)
                     (map #(:input-text %))
                     (reduce str))
         _      (tap> (->> (.getSelection js/window)
                           (.toString)))
         ctx    (get-in db [:sci :ctx])
         result (sci/eval-string* ctx source)]
     (assoc-in db [:sci :global :eval-result] result))))

;; Frame Events
;; For creating new window frames
(re-frame/reg-event-db
 ::render-frame
 (fn [db [_ frame-id component]]
   (let [wrapper (get-in db [:sci :vars :frame-wrapper])
         component [wrapper frame-id component]]
     ;; we check if it exists or not in the db, and if it does we just update the component field, otherwise we make it visible as well
     (if-let [frame (get-in db [:frames (keyword frame-id) :component])]
       (assoc-in db [:frames (keyword frame-id) :component] component)
       (assoc-in db [:frames (keyword frame-id)] {:component component
                                                  :visible?  true})))))

(re-frame/reg-event-db
 ::display-frame
 (fn [db [_ frame-id visible?]]
   (assoc-in db [:frames (keyword frame-id) :visible?] visible?)))

;; Storing Values
(re-frame/reg-event-db
 ::set-var
 (fn [db [_ key value]]
   (if (coll? key)
     (assoc-in db (vec (concat [:sci :vars] key)) value)
     (assoc-in db [:sci :vars key] value))))

(re-frame/reg-event-fx
 ::store-value
 (fn [_cofx [_ key value]]
   {:local-store [key value]}))
