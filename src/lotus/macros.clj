(ns lotus.macros)

(defmacro defhtml [n state & body]
  `(lotus.dom/make-html ~(keyword n)
                        (fn ~state ~@body)))

(defmacro defstate [n & body]
  `(lotus.dom/make-state ~(keyword n) ~@body))
