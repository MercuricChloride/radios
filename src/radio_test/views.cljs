(ns radio-test.views
  (:require
   [radio-test.events :as events]
   [radio-test.subs :as subs]
   [radio-test.shortcuts :refer [rp-example global-eval]]
   [re-com.box :refer [v-box]]
   [re-com.buttons :refer [button]]
   [re-com.core :as re-com :refer [at line]]
   [re-com.input-text :refer [input-textarea]]
   [re-frame.core :as re-frame :refer [dispatch]]
   [re-pressed.core :as rp]))

(defn dispatch-keydown-rules []
  (re-frame/dispatch
   [::rp/set-keydown-rules
    {:event-keys [rp-example ; just a sample shortcut
                  global-eval ; ctrl-e will eval all items in the stations
                  ]
     :clear-keys
     [[{:keyCode 27} ;; escape
       ]]}]))

(defn display-re-pressed-example []
  (let [re-pressed-example (re-frame/subscribe [::subs/re-pressed-example])]
    [:div
     [:p
      "Re-pressed is listening for keydown events. However, re-pressed
      won't trigger any events until you set some keydown rules."]

     [:div
      [re-com/button
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
       [re-com/alert-box
        :src        (at)
        :alert-type :info
        :body       rpe])]))

(defn title []
  (let [name (re-frame/subscribe [::subs/name])]
    [re-com/title
     :src   (at)
     :label (str "Hello from " @name)
     :level :level1
     :attr {:id "something"}]))

(defn sci-interaction
  ([] [sci-interaction (keyword (gensym "station-"))])
  ([key]
   (let [{:keys [input-text eval-result]} @(re-frame/subscribe [::subs/sci-values key])
         project-name @(re-frame/subscribe [::subs/project-name])
         key-string (key->js key)]
     [v-box
      :src (at)
      :height "100%"
      :width "20%"
      :children [[:div {:id (str project-name "." key-string)}
                  [:h2 key-string]]
                 [input-textarea
                  :model input-text
                  :on-change #(dispatch [::events/update-input-text key %])]
                 [button
                  :label "eval"
                  :on-click #(dispatch [::events/eval-sci key input-text])]
                 [:p (or eval-result "Nil result!")]]])))

(defn station-editors
  []
  (let [stations @(re-frame/subscribe [::subs/station-keys])]
    [v-box
     :gap "10px"
     :children [(map (fn [key] [sci-interaction key]) stations)]]))

(defn main-panel []
  (dispatch-keydown-rules)
  [re-com/v-box
   :src      (at)
   :height   "100%"
   :children [[title]
              [:div {:id "test-renderer"} "hi"]
              [v-box
               :src (at)
               :height "100%"
               :width "20%"
               :padding "10px"
               :children [[station-editors]]]]])
