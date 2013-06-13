(ns lotus.html)

(def character-escapes {\& "&amp;", \< "&lt;", \> "&gt;", \" "&quot;"})

(def css-px-attributes
  #{:line-height :top :bottom :left :right :width :height :margin :padding
    :margin-left :margin-right :margin-top :margin-bottom :padding-top :padding-right
    :padding-bottom :padding-left})

(def svg-tags ["svg" "circle" "g" "path" "text" "rect"])

(defn pixels
  [k]
  (format "%.1fpx"  k))

(defn render-css
  [css]
  (apply str
    (interpose \;
      (for [[k v] css]
        (str (name k) ":"
          (cond (keyword? v)
                  (name v)
                (and (number? v)
                 (css-px-attributes k)) (pixels v)
                :else v))))))

(defn make-attributes
  [attrs jsnode]
  (doseq [[k v] attrs]
    (cond
     (= k :style) (.setAttribute jsnode "style" (render-css v))
     (nil? v) nil
     :else (.setAttribute jsnode (name k) v)
     ))
  jsnode)


(defn create-element
  [node]
  (if (some #(= (:tag node) %) svg-tags)
    (.createElementNS js/document "http://www.w3.org/2000/svg" (:tag node))
    (.createElement js/document (:tag node))))

(defn create-textnode
  [node]
  (.createTextNode js/document (:value node)))

(defn make-node
  [node]
  (cond
    (= (:tag node) "string") (create-textnode node)
    :else (create-element node)))

(defn remove-node
  [node]
  (.removeChild (.-parentNode node) node))

(defn change-node
  [el from to text]
  (if text
    (aset el "textContent" to)
    (do
      (doseq [k (keys to)]
        (if (= k :style)
          (.setAttribute el (name k) (render-css (k to)))
          (.setAttribute el (name k) (k to))))
      (doseq [k (keys from)]
        (if (nil? (k to))
          (.removeAttribute el (name k)))))))
