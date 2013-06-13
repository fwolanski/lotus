(ns lotus.dom
  "DOM map normalization utilities."
  (:use [lotus.html :only [make-node remove-node change-node make-attributes]]
        [lotus.diff :only [find-diff-strategy]]
        [lotus.normalize :only [normalize-dom]]
        [lotus.bind :only [make-listeners remove-listeners change-listeners update-event-binders]]))

(def dom-states (atom {}))
(def states (atom {}))
(def templates (atom {}))

(def body-el (.-body js/document))
(def root-element (.createElement js/document "div"))

(.insertBefore body-el root-element (.-firstChild body-el))

(defn get-element
  [base-el level]
  (if (first level)
    (get-element (aget (.-childNodes base-el) (first level)) (rest level))
    base-el))

(defn listen-fn
  [n l-obj event]
  (.stopPropagation event)
  (.preventDefault event)
  (swap! states assoc n (lotus.bind/-listener l-obj (n @states)))
  false)

(defn render-changes
  [change-seq old-dom new-dom base-el state-name]
  (doseq [[ol nl] change-seq]
    (let [oldvals (:value (nth old-dom ol))
          newvals (:value (nth new-dom nl))
          text-el (= (:tag (nth old-dom ol)) "string")
          el (get-element base-el (:level (nth old-dom ol)))]
      (change-node el oldvals
                   (change-listeners el oldvals newvals
                                     (partial listen-fn state-name)
                                     (state-name @states) state-name) text-el))))

(defn render-deletions
  [remove-seq old-dom base-el state-name]
  (doseq [ol (into () remove-seq)]
    (let [el (get-element base-el (:level (nth old-dom ol)))]
      (remove-listeners el state-name)
      (remove-node el))))

(defn render-insertions
  [insert-seq new-dom base-el state-name]
  (doseq [nl insert-seq]
    (let [item (nth new-dom nl)
          parent-el (get-element base-el (pop (:level item)))
          node (make-node item)
          el (make-attributes (make-listeners (:value item)
                                              node
                                              (partial listen-fn state-name)
                                              (state-name @states)
                                              state-name) node)
          children (.-length (.-childNodes parent-el))]
      (cond
        (zero? children) (.appendChild parent-el el)
        (= children (peek (:level item))) (.appendChild parent-el el)
        :else (.insertBefore parent-el
                             el
                             (aget (.-childNodes parent-el) (peek (:level item))))))))

(defn render-diff
  [diff old-dom new-dom base-el state-name]
  (if (map? diff)
    (do
      ;; (.log js/console (str diff) "\n\n" (apply str (map #(str %2 " - " %1 "\n") new-dom (iterate inc 0))))
      (render-changes (:keep diff) old-dom new-dom base-el state-name)
      (render-deletions (:remove diff) old-dom base-el state-name)
      (render-insertions (:insert diff) new-dom base-el state-name))))

(defn dom-update
  [old-dom new-dom state-name]
  (let [base root-element]
      (render-diff (find-diff-strategy old-dom new-dom)
                   old-dom new-dom base state-name)))

(defn make-html
  [state-name body]
  (swap! templates assoc state-name body))

(defn make-state
  [state-name state]
  (swap! states assoc state-name state))

(defn bind-html-and-state
  [html state]
  (dom-update nil
              (html (swap! dom-states assoc html
                           (normalize-dom
                            ((html @templates) (state @states)))))
              html)
  (update-event-binders (state @states) state)
  (add-watch states html
             (fn [key ref old-state new-state ]
               (if (not= (state old-state) (state new-state))
                 (dom-update (html @dom-states)
                             (html (swap! dom-states assoc html
                                          (normalize-dom
                                           ((html @templates) (state new-state)))))
                             html))
               (update-event-binders (state new-state) state))))

(defn force-update-state
  [state-name state]
  (swap! states assoc state-name state))
