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
   [re-frame.core :as re-frame :refer [dispatch subscribe]]
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
  (let [ns-string @ns-string
        {:keys [input-text eval-result]} @(re-frame/subscribe [::subs/sci-values ns-string])]
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

(defn menu-item
  "An item in a menu. Expects a `:title` string, and a `:body` hiccup"
  [& {:keys [title body]}]
  [h-box
   :src (at)
   :justify :between
   :children [[box :child title :align-self :center]
              body]])

(defn frame-context-menu
  "The context menu for a frame.
  Params:
  `:closed` which should be an r/atom
  `:id` string"
  [& {:keys [id closed?]}]
  (let [frame-params {}]
    [v-box
     :padding "10px"
     :gap "10px"
     :width "100%"
     :children [[menu-item
                 :title "Delete:"
                 :body  [button
                         :label "Delete Frame"
                         :on-click #(reset! closed? true)]]
                [menu-item
                 :title "Id:"
                 :body id]]]))

(defn default-frame
  ([child] [default-frame (gensym "frame-") child])
  ([id child-component]
   (let [bg-color         (listen :bg-primary)
         titlebar-bar     (listen :bg-secondary)
         popover-position (listen :popover-position)
         show-body?       (r/atom true)
         show-properties? (r/atom false)
         closed?          (r/atom false)]
     (fn [id child-component]
       (when-not @closed?
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
                        :src      (at)
                        :showing? show-properties?
                        :position @popover-position
                        :popover  [popover-content-wrapper
                                   :src (at)
                                   :title "Frame menu"
                                   :width "300px"
                                   :body [frame-context-menu
                                          :id id
                                          :closed? closed?]]
                        :anchor   [box :child "Title Bar"
                                   :style {:background @titlebar-bar}
                                   :attr  {:on-context-menu #(do (.preventDefault %)
                                                                 (swap! show-properties? not))
                                           :on-double-click #(do (.preventDefault %)
                                                                 (swap! show-body? not))
                                           :class-name      "handle"
                                           :draggable       false}]]
                       (when @show-body?
                         [box :child child-component])]]]])))))

(defn sci-interaction [initial-namespace]
  (let [ns-string (r/atom initial-namespace)
        namespaces (re-frame/subscribe [::subs/sci-namespaces])]
    (fn []
      [:<>
       [:h3 "Editor: "]
       [typeahead
        :data-source (fn [input] (into [] (filter #(str/starts-with? % input) @namespaces)))
        :change-on-blur? true
        :rigid? false
        :model ns-string
        :on-change #(do
                      (.log js/console %)
                      (reset! ns-string %))]
       [sci-editor ns-string]])))

(defn dispatch-default-frames []
  (dispatch [::events/set-var :frame-wrapper default-frame])
  (dispatch [::events/render-frame :default-editor [sci-interaction "example-project.default"]])
  (dispatch [::events/render-frame :greeting [title]]))

(defn main-panel []
  (dispatch-default-frames)
  (dispatch-keydown-rules)
  (let [visible-frames (subscribe [::subs/visible-frames])]
    (fn []
      [re-com/v-box
       :src      (at)
       :height   "100vh"
       :width "100vw"
       :attr {:id "root-frame-panel"
              :on-context-menu #(do
                                  (.preventDefault %)
                                  (.log js/console %))}
       :children @visible-frames
       ;;[default-frame [title]]
       ;; [v-box
       ;;  :src (at)
       ;;  :height "100%"
       ;;  :width "fit-children"
       ;;  :padding "10px"
       ;;  :children [[sci-interaction "example-project.default"]]]
       ])))
