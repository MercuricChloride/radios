(ns radio-test.sci
  (:require
   [re-frame.core :as re-frame :refer [dispatch]]
   [reagent.dom :as rdom]
   [sci.core :as sci :refer [new-dynamic-var]]))

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
  [component]
  (let [root-element (.getElementById js/document @parent-id)]
    (rdom/unmount-component-at-node root-element)
    (rdom/render component root-element)
    nil))

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
                       'render render}
                      bindings)
     :namespaces {'rf (sci/copy-ns re-frame.core nil)}})))
