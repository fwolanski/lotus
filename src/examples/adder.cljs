(ns examples.adder
  (:use-macros
   [lotus.macros :only [defhtml defstate]])
  (:use [lotus.dom :only [bind-html-and-state]]))

(defstate adder
  {:a 5
   :b 5})

(defn change-value
  [v state node & update]
  (if update
    (set! (.-value node) (state v))
    (let [val (.-value node)
          valid (re-matches #"^[0-9]+$" val)]
      (if valid
        (assoc state v (js/parseInt val))
        state))))

(defhtml adder [s]
  [:div.container
   [:input {:bind-value (partial change-value :a) :value (:a s)}]
   " plus "
   [:input {:bind-value (partial change-value :b) :value (:b s)}]
   " equals " (+ (:a s) (:b s))])

;; (bind-html-and-state :adder :adder)
