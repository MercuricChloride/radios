(ns radio-test.shortcuts
  (:require [radio-test.events :as events]
            [re-pressed.core :as rp]))

;; NOTE Typing hello, expands to hello world!
(def rp-example
  [[::events/eval-all]
   [{:keyCode 72} ;; h
    {:keyCode 69} ;; e
    {:keyCode 76} ;; l
    {:keyCode 76} ;; l
    {:keyCode 79} ;; o
    ]])

(def global-eval
  [[::events/eval-all]
   [{:keyCode 69 ;e... nice
     :ctrlKey true}]])
