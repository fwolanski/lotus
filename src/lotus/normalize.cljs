(ns lotus.normalize)

(def re-tag
  #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(defn as-str
  "Converts the input variable to a string."
  [x]
  (if (or (keyword? x) (symbol? x))
    (name x)
    (str x)))

(defn parse-tag
  [tag]
  (re-matches re-tag (as-str tag)))

(def parse-tag-mem (memoize parse-tag))

(defn make-strings
  [s]
  (map #(if (coll? %) % (str %)) s))

(defn join-strings
  [x]
  (mapcat
    #(cond
       (string? (first %)) (list (apply str %))
       (seq? (first %)) (apply concat %)
       :else % )
    (partition-by string? (make-strings x))))

(declare normalize-map)

(defn normalize-tag
  [[tag & content]]
  (when (not (or (keyword? tag) (symbol? tag)))
    (throw (str tag " is not a valid tag name")))
  (let [[_ tag id class] (parse-tag-mem tag)
        tag-attrs {:id (if (and (map? (first content))
                                (contains? (first content) :id))
                         nil id)
                   :class (if class (str (.replace ^String class "." " ") " "))}
        map-attrs (first content)]
    (letfn [(flatten-seqs [to m] (if (seq? m) (apply conj to m) (conj to m)))]
      (if (map? map-attrs)
        (apply conj
          [{:tag tag :value (merge-with str tag-attrs map-attrs)}]
          (reduce flatten-seqs [] (map normalize-map (join-strings (rest content)))))
        (apply conj
          [{:tag tag :value tag-attrs}]
          (reduce flatten-seqs [] (map normalize-map (join-strings content))))))))

(defn normalize-map
  [m]
  (cond
    (vector? m) (normalize-tag m)
    (seq? m) (map normalize-tag m)
    :else [{:tag "string" :value (str m)}]))


(defn flatten-tree
  [t level]
  (mapcat #(if (map? %)
         (vector (merge %1 {:level level}))
         (flatten-tree %1 (conj level %2))) t (iterate inc -1)))

(defn normalize-dom
  [t]
  (if t (flatten-tree (normalize-map t) [0])))
