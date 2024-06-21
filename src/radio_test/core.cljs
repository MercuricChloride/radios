(ns radio-test.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [re-pressed.core :as rp]
   [radio-test.events :as events]
   [radio-test.views :as views]
   [radio-test.config :as config]
   [devtools.core :as devtools]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")
    (devtools/install!)))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (views/dispatch-default-frames)
  ;;(re-frame/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])
  (dev-setup)
  (mount-root))
