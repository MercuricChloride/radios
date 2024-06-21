(ns radio-test.editor
  (:require
   ["@codemirror/commands" :refer [history historyKeymap]]
   ["@codemirror/language" :refer [defaultHighlightStyle foldGutter syntaxHighlighting]]
   ["@codemirror/state" :refer [EditorState]]
   ["@codemirror/view" :as view :refer [drawSelection EditorView updateListener]]
   ["@nextjournal/clojure-mode" :refer [complete_keymap default_extensions]]
   ["react" :as react]
   [applied-science.js-interop :as j]
   [re-frame.core :refer [subscribe]]
   [reagent.core :as r]))

(def theme
  (.theme EditorView
          (j/lit {".cm-content"             {:white-space "pre-wrap"
                                             :padding     "10px 0"
                                             :flex        "1 1 0"}
                  "&"                       {:width  "100%"
                                             :height "100%"}
                  "&.cm-focused"            {:outline "0 !important"}
                  ".cm-scroller"            {:overflow "auto"}
                  ".cm-line"                {:padding     "0 9px"
                                             :line-height "1.6"
                                             :font-size   "16px"
                                             :font-family "var(--code-font)"}
                  ".cm-matchingBracket"     {:border-bottom "1px solid var(--teal-color)"
                                             :color         "inherit"}
                  ".cm-gutters"             {:background "transparent"
                                             :border     "none"}
                  ".cm-gutterElement"       {:margin-left "5px"}
                  ;; only show cursor when focused
                  ".cm-cursor"              {:visibility "hidden"}
                  "&.cm-focused .cm-cursor" {:visibility "visible"}})))

(def extensions #js[theme
                    (history)
                    (view/lineNumbers)
                    (syntaxHighlighting defaultHighlightStyle)
                    default_extensions
                    (drawSelection)
                    (foldGutter)
                    (.. EditorState -allowMultipleSelections (of true))
                    (.of view/keymap complete_keymap)
                    (.of view/keymap historyKeymap)
                    (.of (.-updateListener EditorView)
                         (fn [^js/Object v]
                           (when (.-docChanged v)
                             (.log js/console "updated"))))])

;; TODO Update this to add extensions for saving the state to the db
(defonce db-interop-extensions #js {})

(defn code-mirror-to-string
  [])

(defn editor-state
  [input-text]
  (.create EditorState
           #js {"doc" input-text
                "extensions" extensions}))

(defn editor
  [& {:keys [ns-string]}]
  (let [ns-state (subscribe [:radio-test.subs/sci-values ns-string])
        !view    (r/atom nil)
        mount!   (fn [el] (reset! !view
                                  (new EditorView
                                       (j/obj :state (editor-state (:input-text @ns-state))
                                              :parent el))))]
    [:div#codemiror-wrapper
     {:ref mount!}]))
