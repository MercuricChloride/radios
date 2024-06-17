(ns radio-test.events
  (:require
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [radio-test.db :as db]
   [radio-test.sci :refer [init-context parent-id]]
   [re-frame.core :as re-frame :refer [inject-cofx]]
   [sci.core :as sci]
   [reagent.dom :as rdom]))

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

;; This will grab an element from the dom, and store it in "root-element" in the cofx map
(re-frame/reg-cofx
 :root-element
 (fn [cfx id]
   (assoc cfx :root-element (.getElementById js/document (clj->js id)))))

;; This will create a new frame in the app
(re-frame/reg-fx
 :create-frame
 (fn [[frame-id component]]
   (let [app-root (.getElementById js/document "app")
         el       (.createElement js/document "div")]
     (set! (.-id el) frame-id)
     (.appendChild app-root el)
     (rdom/unmount-component-at-node el)
     (rdom/render (or component [:h1 "hello!"]) el))))

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

(add-tap #(.log js/console %))

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
(re-frame/reg-event-fx
 ::create-frame
 [(inject-cofx :root-element)]
 (fn [_cfx [_ frame-id component]]
   {:create-frame [frame-id component]}))

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
