(ns radio-test.re-pressed
  (:require
   [radio-test.shortcuts :refer [global-eval rp-example]]
   [radio-test.subs :as subs]
   [re-com.core :refer [alert-box at button]]
   [re-frame.core :as re-frame :refer [subscribe dispatch]]
   [re-pressed.core :as rp]))

(defn dispatch-keydown-rules []
  (dispatch
   [::rp/set-keydown-rules
    {:event-keys [rp-example        ; just a sample shortcut
                  global-eval       ; ctrl-e will eval all items in the stations
                  ]
     :clear-keys
     [[{:keyCode 27} ;; escape
       ]]}]))

(defn display-re-pressed-example []
  (let [re-pressed-example (subscribe [::subs/re-pressed-example])]
    [:div
     [:p
      "Re-pressed is listening for keydown events. However, re-pressed
      won't trigger any events until you set some keydown rules."]

     [:div
      [button
       :src      (at)
       :on-click dispatch-keydown-rules
       :label    "set keydown rules"]]

     [:p
      [:span
       "After clicking the button, you will have defined a rule that
       will display a message when you type "]
      [:strong [:code "hello"]]
      [:span ". So go ahead, try it out!"]]

     (when-let [rpe @re-pressed-example]
       [alert-box
        :src        (at)
        :alert-type :info
        :body       rpe])]))
