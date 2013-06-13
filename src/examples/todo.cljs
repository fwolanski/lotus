(ns examples.todo
  (:use-macros
   [lotus.macros :only [defhtml defstate]])
  (:use [lotus.dom :only [bind-html-and-state]]))

(defstate todo
  {:todos [ {:title "Task One" :completed false}
            {:title "Task Two" :completed false} ]
   :newtodo ""})

(defn add-todo
  [state]
  (-> state
      (assoc-in [:todos (count (:todos state))]
                {:title (:newtodo state) :completed false})
      (assoc :newtodo "")))

(defn clear-done
  [state]
  (assoc state :todos
         (into [] (filter (complement :completed) (:todos state)))))

(defn render-todo [t idx]
  [:li
   [:input {:type "checkbox" :bind-checked [:todos idx :completed]}] " "
   [:span (if (:completed t) {:style {:text-decoration "line-through"
                                      :color "gray"}}) (inc idx) ". " (:title t)]])

(defhtml todo [s]
  [:div.container
   [:h1 "Todos"]
   (let [complete (count (filter :completed (:todos s)))
         total (count (:todos s))]
     [:div
      (if (= complete total) "All done. " (str complete " of " total " done. "))
      (if (> complete 0) [:button.btn.btn-mini {:bind-click clear-done :style
                                               {:cursor "pointer"}} "Clear"])])
   [:ul.unstyled (map render-todo (:todos s) (iterate inc 0))]
   [:form {:bind-submit add-todo} [:input {:bind-value :newtodo}] [:button.btn "Add" ]]])

;; (bind-html-and-state :todo :todo)
