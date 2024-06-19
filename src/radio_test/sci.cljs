(ns radio-test.sci
  (:require
   [re-frame.core :as re-frame :refer [dispatch]]
   [reagent.dom :as rdom]
   [sci.core :as sci :refer [new-dynamic-var]]
   [re-com.core :as re-com]
   [cljs-http.core :as https]
   [reagent.core :as r]
   [shadow.esm :refer (dynamic-import)]
   [shadow.cljs.modern :refer (js-await)]))

;; We added support for managing sci contexts
;; Specifically, these contexts can communicate via a central db, and can render their own contents.

(defn emit
  [channel value]
  (dispatch [:radio-test.events/set-var channel value]))

(defn listen
  [& keys]
  (re-frame/subscribe [:radio-test.subs/get-var keys]))

;; Dynamic Variables
(def parent-id
  "A dynamic variable which represents the unique of the component we want to attach to in the `render` function"
  (new-dynamic-var  '*parent-id* "test-renderer"))

(defn render
  ([component]
   (let [parent-id @parent-id]
     (render component parent-id)))
  ([component parent-id]
   (dispatch [:radio-test.events/render-frame parent-id component])))

(defn load-script [url cb]
  (js-await [mod (dynamic-import url)]
            (cb mod)))

(defn init-context
  ([] (init-context {}))
  ([{:keys [bindings]}]
   (sci/init
    {:bindings (merge {'emit emit
                       'listen listen
                       'print #(dispatch [:radio-test.events/log %])
                       'reset #(dispatch [:radio-test.events/reset-context])
                       'create-frame #(dispatch [:radio-test.events/create-frame %])
                       '*parent-id* parent-id
                       'render render
                       'import load-script}
                      bindings)
     :namespaces {'rf (sci/copy-ns re-frame.core nil)
                  're-com (sci/copy-ns re-com.core nil)
                  'reagent (sci/copy-ns reagent.core nil)}
     :classes {'js js/globalThis :allow :all}})))
