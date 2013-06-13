(ns examples.clock
  (:use-macros
   [lotus.macros :only [defhtml defstate]])
  (:use [lotus.dom :only [bind-html-and-state force-update-state]]))

(defn gen-time []
  (let [date (js.Date.)]
    {:s  (.getSeconds date)
     :h (rem (.getHours date) 12)
     :m  (.getMinutes date)
     :a (quot (.getHours date) 12)
     :emph (if (= 0 (.getSeconds date)) true false)}))

(defn time-to-angle
  [domain value]
  (- (* 2 (.-PI js/Math) (/ value domain)) (/ (.-PI js/Math) 2)))

(defstate clock
  (gen-time))

(defn render-hand
  [angle radius width]
  (let [y (* radius (.sin js/Math angle))
        x (* radius (.cos js/Math angle))]
    [:path {:d (str "m0,0L" x "," y "Z")
            :stroke "black"
            :stroke-width width
            :fill "none"}]))

(defn render-hours
  [radius]
  (for [h (take 12 (iterate inc 1)) ]
    (let [angle (time-to-angle 12 h)
          y (* radius (.sin js/Math angle))
          x (* radius (.cos js/Math angle))]
      [:path {:d (str "m" x "," y "L" (* x 1.1) "," (* y 1.1) "Z")
              :stroke "black" :stroke-width 2 }])))

(defn render-clock
  [state diff pos title color]
  (let [s (-> state
              (assoc :h (mod (+ (:h state) diff) 12))
              (assoc :a (quot (+ (:h state) diff) 12)))]
    [:g {:transform  (str "translate(" (+ (* 200 pos) 150) ",120)") }
     [:circle {:r 85 :fill color :stroke "black" :stroke-width 2}]
     [:circle {:r 80 :fill (if (:emph s) color  "#f1f1f1")
               :stroke "black" :stroke-width 1}]
     [:circle {:r 50 :fill color :stroke "black" :stroke-width 1}]
     [:circle {:r 45 :fill "#f1f1f1" :stroke "black" :stroke-width 1}]
     [:circle {:r 4 :fill "black"}]
     (render-hand (time-to-angle 12 (:h s)) 45 4)
     (render-hand (time-to-angle 60 (:m s)) 65 3)
     (render-hand (time-to-angle 60 (:s s)) 80 2)
     (render-hours 60)
     [:text {:x 0 :y 110 :text-anchor "middle"}
      (format "%d:%02d:%02d"  (:h s) (:m s) (:s s))
      " " (if (= (:a s) 1) "AM" "PM") ]
     [:text {:x 0 :y -100 :text-anchor "middle" :font-weight "bold"} title]]))

(defhtml clock [s]
  [:center
   [:svg {:width 900 :height 250}
    (render-clock s 0   0 "Montreal" "#f1f1aa")
    (render-clock s -3  1 "San Francisco" "#f1aaf1")
    (render-clock s 5   2 "London" "#aaf1f1")
    (render-clock s 10  3 "Bombay" "#f1aaaa")]])

;; (bind-html-and-state :clock :clock)
;; (js/setInterval (fn [] (force-update-state :clock (gen-time))) 500)
