(ns radio-test.db
  (:require
   [cljs.pprint :refer [cl-format]]
   [radio-test.sci :refer [init-context]]
   [clojure.string :as string]))

(defonce context (init-context))

(defn make-station [project-name key input-text]
  {:input-text (cl-format nil "(ns ~a.~a ~%  (:require [user :refer :all]))~%~%~a" project-name (key->js key) input-text)
   :eval-result nil})

(defn get-project-name
  []
  (-> (.. js/window -location -pathname)
      (string/split #"project/")
      (second)))

(def default-db
  (let [project-name (get-project-name)]
    {:name "radio-playground 📻"
     :project-name project-name
     :sci {:ctx context
           :stations {:default (make-station project-name :default "(emit :some-value 123) 123")
                      :another (make-station project-name :another "@(listen :some-value)")
                      :renderer (make-station project-name :renderer "(defn test-component []
    (let [input @(listen :some-value)]
    [:div
      [:h1 \"The value is:\"]
      [:h1 input]]))

(defn example-button []
    [:button {:on-click #(emit :some-value 42069)} \"Click me!\"])

(render [test-component])")}
           :global {:eval-result nil}
           :channels {}}}))
