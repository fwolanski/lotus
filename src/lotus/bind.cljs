(ns lotus.bind
  (:require [goog.events :as gevents]))

(def listeners (atom {}))
(def event-bindings (atom {}))

(defprotocol LotusListener
  (-binder [this listen-fn])
  (-listener [this state])
  (-updater [this state]))

(defn update-event-binders
  [state state-name]
  (doseq [s (state-name @event-bindings)]
    (-updater s state)))

(defn add-binder
  [obj options node listen-fn state state-name]
  (let [ln (obj. node options)]
    (-binder ln (partial listen-fn ln))
    (swap! event-bindings assoc state-name
           (into [] (conj (state-name @event-bindings) ln)))))

(defn make-listeners
  [attr-map node listen-fn state state-name]
  (if (map? attr-map)
    (loop [attrs {}
           key (keys attr-map)]
      (if (empty? key)
        attrs
        (if (nil? ((first key) @listeners))
          (recur (assoc attrs (first key) ((first key) attr-map)) (rest key))
          (do
            (add-binder ((first key) @listeners)
                        ((first key) attr-map)
                        node listen-fn state state-name)
            (recur attrs (rest key))))))))

(defn remove-listeners
  [node state-name]
  (gevents/removeAll node)
  (swap! event-bindings assoc state-name
         (into [] (filter #(not= node (.-node %1)) (state-name @event-bindings)))))

(defn change-listeners
  [node old-atts new-attr listen-fn state state-name]
  (if (and (map? old-atts) (map? new-attr))
    (do
      (doseq [k (keys old-atts)]
        (if (k @listeners)
          (if (not= (k old-atts) (k new-attr))
            (remove-listeners node state-name))))
      (loop [attrs {}
             key (keys new-attr)]
        (if (empty? key)
          attrs
          (if (nil? ((first key) @listeners))
            (recur (assoc attrs (first key) ((first key) new-attr)) (rest key))
            (if (not= ((first key) old-atts) ((first key) new-attr))
              (do
                (add-binder ((first key) @listeners)
                             ((first key) new-attr)
                             node listen-fn state state-name)
                (recur attrs (rest key)))
              (recur attrs (rest key)))))))
    new-attr))

(defn register-listener
  [listener attr]
  (swap! listeners assoc attr listener))

(defn set-state
  [state options value]
  (cond
   (vector? options) (assoc-in state options value)
   (keyword? options) (assoc state options value)))

(defn get-options
  [state options]
  (cond
   (vector? options) (get-in state options)
   (keyword? options) (get state options)))
