(ns radio-test.db
  (:require
   [cljs.pprint :refer [cl-format]]
   [clojure.string :as string]
   [radio-test.sci :refer [init-context]]))

(defonce context (init-context))

(defn make-station
  "Loads a station from localStorage or uses a `input-test` if it doesn't exist"
  [namespace input-text]
  (let [namespace  (key->js namespace)
        input-text (or (.getItem js/localStorage namespace)
                       (cl-format nil "(ns ~a ~%  (:require [user :refer :all]))~%~%~a" namespace input-text))]
    {:input-text  input-text
     :eval-result nil}))

(defn get-project-name
  []
  (-> (.. js/window -location -pathname)
      (string/split #"project/")
      (second)))

(def default-styles
  {:bg-primary "white"
   :bg-secondary "grey"})

(def default-global-values
  {:advanced-keys false
   :popover-position :above-center})

(def default-namespaces
  {:example-project.default  (make-station :example-project.default "(emit :some-value 123)")
   :example-project.another  (make-station :example-project.another "@(listen :some-value)")
   :example-project.renderer (make-station :example-project.renderer "(ns example-project.renderer
  (:require [user :refer :all]
            [reagent :as r]
            [re-com :as rc]))

(def frame-id \"frame-4\")

(defn test-component []
  (let [input      (listen :some-value)
        text-input (r/atom \"\")]
    (fn []
      [:div
       [:h1 \"The value is: \" @input]
       [:h1 \"text input: \" @text-input]
       [rc/input-text :model text-input
        :on-change #(do (reset! text-input %)
                        (emit :some-value %))]])))

(defn example-button []
  [:button {:on-click #(emit :some-value 123123)} \"Reset to 123!\"])

(binding [*parent-id* frame-id]
  (render [:div [test-component]
           [example-button]]))
;;(emit :some-value 429)
")})

(def default-db
  (let [project-name (get-project-name)
        namespaces   (merge default-namespaces)]
    {:name         "radio-playground 📻"
     :project-name project-name
     :namespaces   namespaces
     :sci          {:ctx      context
                    :global   {:eval-result   nil
                               :shell-visible nil}
                    :channels {}
                    :vars     (merge default-styles
                                     default-global-values)}
     :frames {}}))
