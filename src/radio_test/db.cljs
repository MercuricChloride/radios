(ns radio-test.db
  (:require
   [cljs.pprint :refer [cl-format]]
   [radio-test.sci :refer [init-context]]
   [clojure.string :as string]))

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

(def default-db
  (let [project-name (get-project-name)]
    {:name         "radio-playground 📻"
     :project-name project-name
     :namespaces   {:example-project.default  (make-station :example-project.default "(emit :some-value 123)")
                    :example-project.another  (make-station :example-project.another "@(listen :some-value)")
                    :example-project.renderer (make-station :example-project.renderer "(defn test-component []
    (let [input @(listen :some-value)]
    [:div
      [:h1 \"The value is:\"]
      [:h1 input]]))

(defn example-button []
    [:button {:on-click #(emit :some-value 42069)} \"Click me!\"])

(render [test-component])")}
     :sci {:ctx      context
           :global   {:eval-result   nil
                      :shell-visible nil}
           :channels {}
           :vars     (merge default-styles
                            default-global-values)}}))
