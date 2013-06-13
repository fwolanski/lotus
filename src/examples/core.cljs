(ns examples.ex
  (:use-macros
   [lotus.macros :only [defhtml defstate]])
  (:use [lotus.dom :only [bind-html-and-state]]))


;; the state is only a name object
(defstate ex
  {:name "Filip" })

;; the html, which is updated dynamically when the state changes
(defhtml ex [s]
  [:div.container
   [:h1 "Hello " (:name s)]
   [:input {:bind-value :name}]
   (for [a (seq (:name s))]
     [:div a])])

(bind-html-and-state :ex :ex)
