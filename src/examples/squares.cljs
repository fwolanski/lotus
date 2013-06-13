(ns examples.squares
  (:use-macros
   [lotus.macros :only [defhtml defstate]])
  (:use [lotus.dom :only [bind-html-and-state force-update-state]]))

(def time (.getTime (js/Date.)))
(def radius 65)
(def width 16)

(defn rotate []
  (let [node (.getElementById js/document "rotator")
        now (.getTime (js/Date.))
        rate (* 10 (/ (- time now) 1000))
        revs (if (> (- 360 rate) 0 ) 1 0)
        scale (+ 0.7 (* 0.3 (.sin js/Math (/ rate 40))))]
    (.setAttribute node "transform" (str "translate(350,350)rotate("
                                         (- rate (* revs 360)) ")scale(" scale ")"))))

(defn rot-each [node]
  (let [rot (.-dataRot (.-attributes node))
        tran (.-dataTran (.-attributes node))
        now (.getTime (js/Date.))
        rate (* 50 (/ (- time now) 1000))
        revs (if (> (- 360 rate) 0 ) 1 0)]
    (.setAttribute node "transform" (str "rotate("
                                         (- rate (* revs 360)) ")"))))

(defn to-rot []
  (let [nodes (.getElementsByClassName js/document "toRot")
        arr (.call (.-slice (.-prototype js/Array)) nodes)]
    (.forEach arr rot-each)))

(defstate squares
  {:squares (into [] (for [x (range 1 6)]
                       {:radius (* x radius) :width width}))})

(defn render-squares [s]
  (let [dim (/ (* -1 (:width s)) 2)
        num (.floor js/Math (* 2 (.-PI js/Math) (/ (:radius s) (:width s))))
        k (/ 360 num)]
    (for [x (range 1 (inc num))]
      [:g {:transform (str "rotate(" (* x k) ")translate(" (:radius s) ")")}
       [:rect.toRot {:width (:width s) :height (:width s) :x dim :y dim
                     :fill (if (= (mod x 2) 0) "none" "black")
                     :stroke "black" :stroke-width 1
                     :dataRot (* x k)
                     :dataTran (:radius s)} ]])))

(defhtml squares [s]
  [:center
   [:svg {:width 700 :height 700}
    [:g#rotator {:transform "translate(350,350)"}
     (map render-squares (:squares s))]]])

;; (bind-html-and-state :squares :squares)
;; (js/setInterval rotate 20)
;; (js/setInterval to-rot 20)
