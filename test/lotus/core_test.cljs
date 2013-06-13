(ns lotus.core-test
  (:require-macros [lotus.test-macros :as t])
  (:require [lotus.normalize :as n]
            [goog.testing.jsunit :as jsunit]))


(def b1
  [:div "This is a test"
    (for [x (range 5)]
      [:div#id.class1 "number: " x [:bold "doubled: " (* 2 x)] " and " (* 3 x) ])])

(def b2
  [:div "This is a test"
    (for [x (range 5)]
      [:div {:id "id" :class "class1 " } "num" "ber: " x
       [:bold "doubled: " (* 2 x)]
       " and " (* 3 x) ])])


(t/deftest normalization
  (t/is (= (n/normalize-map b1) (n/normalize-map b2))))

