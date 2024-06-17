(ns radio-test.views
  (:require
   ["react-draggable" :as -draggable]
   ["react-resizable" :refer [ResizableBox]]
   [clojure.string :as str]
   [radio-test.events :as events]
   [radio-test.re-pressed :refer [dispatch-keydown-rules]]
   [radio-test.sci :refer [listen]]
   [radio-test.subs :as subs]
   [re-com.box :refer [h-box v-box]]
   [re-com.buttons :refer [button]]
   [re-com.core :as re-com :refer [at box popover-anchor-wrapper
                                   popover-content-wrapper typeahead]]
   [re-com.input-text :refer [input-textarea]]
   [re-frame.core :as re-frame :refer [dispatch]]
   [reagent.core :as r]))

(def draggable (r/adapt-react-class -draggable))
(def resizable (r/adapt-react-class ResizableBox))

(defn title []
  (let [name (re-frame/subscribe [::subs/name])]
    [re-com/title
     :src   (at)
     :label (str "Hello from " @name)
     :level :level1
     :attr {:id "something"}]))

(defn sci-editor
  [ns-string]
  (let [{:keys [input-text eval-result]} @(re-frame/subscribe [::subs/sci-values ns-string])]
    [v-box
     :gap "10px"
     :children [[input-textarea
                 :model input-text
                 :rows 7
                 :change-on-blur? true
                 :on-change #(dispatch [::events/update-input-text ns-string %])]
                [h-box
                 :justify :between
                 :children [[button
                             :label "eval"
                             :on-click #(dispatch [::events/eval-sci ns-string input-text])]
                            [button
                             :label "save"
                             :on-click #(dispatch [::events/store-value ns-string input-text])]
                            [button
                             :label "load"
                             :on-click #(dispatch [::events/load-input-text])]]]
                [:p (or eval-result "Nil result!")]]]))

(defn sci-interaction [initial-namespace]
  (let [bg-color (listen :bg-primary)
        ns-string (r/atom initial-namespace)
        namespaces (re-frame/subscribe [::subs/sci-namespaces])]
    (fn []
      [draggable
       [:div
        {:style {:position "absolute"
                 :border "2px solid"}}
        [v-box
         :style {:background @bg-color}
         :src (at)
         :size "grow"
         :height "400"
         :min-width "300"
         :padding "10px"
         :gap "10px"
         :children [[:h3 "Editor"]
                    ;;[box :attr {:id ns-string} :child ""]
                    [typeahead
                     :data-source (fn [input] (into [] (filter #(str/starts-with? % input) @namespaces)))
                     :change-on-blur? true
                     :rigid? false
                     :model ns-string
                     :on-change #(do
                                   (.log js/console %)
                                   (reset! ns-string %))]
                    [sci-editor @ns-string]]]]])))

(defn default-frame
  ([child] [default-frame (gensym "frame-") child])
  ([id child]
   (let [bg-color (listen :bg-primary)
         titlebar-bar (listen :bg-secondary)
         popover-position (listen :popover-position)
         show-body? (r/atom true)
         show-properties? (r/atom false)]
     (fn []
       [draggable
        {:handle ".handle"}
        [:div
         {:style {:position "absolute"
                  :border   "2px solid"}}
         [v-box
          :style {:background @bg-color}
          :src (at)
          :size "grow"
          :height "400"
          :min-width "300"
          :gap "10px"
          :children [[popover-anchor-wrapper
                      :src (at)
                      :showing? show-properties?
                      :position @popover-position
                      :popover [popover-content-wrapper
                                :src (at)
                                :title (str "Properties for frame: " id)]
                      :anchor [box :child "Title Bar"
                               ;;                      :height "10px"
                               :style {:background @titlebar-bar}
                               :attr {:on-context-menu #(do
                                                          (.preventDefault %)
                                                          (swap! show-properties? not))
                                      :on-double-click #(do
                                                          (.preventDefault %)
                                                          (swap! show-body? not))
                                      :class-name "handle"
                                      :draggable false}]]

                     (when @show-body? child)]]]]))))

(defn main-panel []
  (dispatch-keydown-rules)
  [re-com/v-box
   :src      (at)
   :height   "100vh"
   :width "100vw"
   :attr {:id "root-frame-panel"
          :on-context-menu #(do
                              (.preventDefault %)
                              (.log js/console %))}
   :children [[default-frame [title]]
              [v-box
               :src (at)
               :height "100%"
               :width "fit-children"
               :padding "10px"
               :children [[sci-interaction "example-project.default"]]]]])
