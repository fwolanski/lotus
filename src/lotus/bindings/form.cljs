(ns lotus.bindings.form
  (:use [lotus.bind :only [LotusListener register-listener set-state get-options]])
  (:require [goog.events :as gevents]))

(deftype Value [node options]
  LotusListener
  (-binder [this listen-fn]
    (gevents/listen node (array "input") listen-fn))
  (-updater [this state]
    (if (fn? options)
      (options state node :update)
      (set! (.-value node) (get-options state options))))
  (-listener [_ state]
    (if (fn? options)
      (options state node)
      (set-state state options (.-value node)))))

(register-listener Value :bind-value)

(deftype Checked [node options]
  LotusListener
  (-binder [this listen-fn]
    (gevents/listen node (array "change") listen-fn true))
  (-updater [this state]
    (if (not (fn? options))
      (set! (.-checked node) (get-options state options))))
  (-listener [_ state]
    (if (fn? options)
      (options state node)
      (set-state state options (.-checked node)))))

(register-listener Checked :bind-checked)

(deftype Clicked [node options]
  LotusListener
  (-binder [this listen-fn]
    (gevents/listen node (array "click") listen-fn))
  (-updater [this state] nil)
  (-listener [_ state]
    (if (fn? options)
      (options state node)
      (set-state state options true))))

(register-listener Clicked :bind-click)

(deftype DblClicked [node options]
  LotusListener
  (-binder [this listen-fn]
    (gevents/listen node (array "dblclick") listen-fn))
  (-updater [this state] nil)
  (-listener [_ state]
    (if (fn? options)
      (options state node)
      (set-state state options true))))

(register-listener DblClicked :bind-dblclick)

(deftype Submit [node options]
  LotusListener
  (-binder [this listen-fn]
    (gevents/listen node (array "submit") listen-fn))
  (-updater [this state] nil)
  (-listener [_ state]
    (if (fn? options)
      (options state node)
      (set-state state options true))))

(register-listener Submit :bind-submit)
