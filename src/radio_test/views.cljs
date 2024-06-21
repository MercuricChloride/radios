(ns radio-test.views
  (:require
   ["react-draggable" :as -draggable]
   ["react-resizable" :refer [ResizableBox]]
   [clojure.string :as str]
   [radio-test.editor :refer [editor]]
   [radio-test.events :as events]
   [radio-test.re-pressed :refer [dispatch-keydown-rules]]
   [radio-test.sci :refer [listen]]
   [radio-test.subs :as subs]
   [re-com.box :refer [h-box v-box]]
   [re-com.buttons :refer [button]]
   [re-com.core :as re-com :refer [at box popover-anchor-wrapper
                                   popover-content-wrapper typeahead]]
   [re-frame.core :as re-frame :refer [dispatch subscribe]]
   [reagent.core :as r]))

(def draggable (r/adapt-react-class -draggable))
(def resizable (r/adapt-react-class ResizableBox))

(defn title []
  (let [name (re-frame/subscribe [::subs/name])]
    [re-com/title
     :src   (at)
     :label (str "Hello from " @name)
     :level :level1]))

(defn sci-editor
  [ns-string]
  (let [ns-string @ns-string
        {:keys [input-text eval-result]} @(re-frame/subscribe [::subs/sci-values ns-string])]
    [v-box
;;     :height "100%"
;;     :width "100%"
     :gap "10px"
     :size "shrink"
     :children [[editor :ns-string input-text]
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
  `:id` keyword"
  [& {:keys [id]}]
  (let [frame-params {}]
    [v-box
     :gap "10px"
     :width "100%"
     :children [[menu-item
                 :title "Delete:"
                 :body  [button
                         :label "Delete Frame"
                         :on-click #(dispatch [::events/set-frame-visible id false])]]
                [menu-item
                 :title "Id:"
                 :body id]]]))

(defn draggable-frame [id child]
  (let [pos (subscribe [::subs/frame-pos id])
        hovered? (r/atom false)
        dragging? (r/atom false)
        bg-primary (listen :bg-primary)]
    (fn []
      [box
       :size "1"
       :style {:position "absolute"
               :left (:x @pos)
               :top (:y @pos)
               :background @bg-primary
               :border (if @hovered?
                         "4px solid"
                         "2px solid")}
       :attr {:on-drag-start #(reset! dragging? true)
              :on-drag-end #(do (reset! dragging? false)
                                (dispatch [::events/update-frame-pos id (.-clientX %) (.-clientY %)]))
              :draggable true
              :on-mouse-over #(reset! hovered? true)
              :on-mouse-out #(reset! hovered? false)}
       :child child])))

(defn default-frame
  ([child] [default-frame (gensym "frame-") child])
  ([id child-component]
   (let [bg-color         (listen :bg-primary)
         titlebar-bar     (listen :bg-secondary)
         popover-position (listen :popover-position)
         show-body?       (r/atom true)
         show-properties? (r/atom false)]
     (fn []
       [draggable-frame
        id
        [v-box
         :children
         [[popover-anchor-wrapper
           :src      (at)
           :showing? show-properties?
           :position @popover-position
           :popover  [popover-content-wrapper
                      :src (at)
                      :title "Frame menu"
                      :width "300px"
                      :height "fit-content"
                      :body [frame-context-menu
                             :id id]]
           :anchor   [box
                      :size "30px"
                      :justify :center
                      :align :center
                      :child (str (clj->js id))
                      :style           {:background @titlebar-bar
                                        :width      "100%"}
                      :attr {:on-context-menu #(do (.preventDefault %)
                                                   (swap! show-properties? not))
                             :on-double-click #(do (js/console.log %)
                                                   (js/console.log @show-body?)
                                                   (.preventDefault %)
                                                   (swap! show-body? not))}]]
          [:<> child-component]]]]))))

(defn sci-interaction [initial-namespace]
  (let [ns-string (r/atom initial-namespace)
        namespaces (re-frame/subscribe [::subs/sci-namespaces])]
    (fn []
      [:<>
       [typeahead
        :data-source (fn [input] (into [] (filter #(str/starts-with? % input) @namespaces)))
        :change-on-blur? true
        :rigid? false
        :model ns-string
        :width "100%"
        :height "100%"
        :on-change #(reset! ns-string %)]
       [sci-editor ns-string]])))

(defn dispatch-default-frames []
  (dispatch [::events/set-var :frame-wrapper default-frame])
  (dispatch [::events/set-var :visible-fn #(:visible? %)])
  (dispatch [::events/set-var :render-fn #(:component %)])
  (dispatch [::events/render-frame :default-editor [sci-interaction "startup"]])
  (dispatch [::events/render-frame :greeting [title]])
  (dispatch [::events/eval-ns :startup]))

(defn main-panel []
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
       :children @visible-frames])))
