(ns lotus.diff
  (:use [lotus.normalize :only [normalize-dom]] ))

(defn lcs-matrix
  [s t]
  (let [rows (inc (count s))
        cols (inc (count t))
        a (apply array (repeat rows (apply array (repeat cols 0)) ))]
    (doseq [i (range 1 rows)]
      (doseq [j (range 1 cols)]
        (if (= (nth s (dec i)) (nth t (dec j)))
          (aset (get a i) j (inc (aget a (dec i) (dec j))))
          (aset (get a i) j (max (aget a i (dec j))
                           (aget a (dec i) j))))))
    a))

(defn lcs-diff
  [s t]
  (let [m (lcs-matrix s t)
        f (fn lcs-diff-rec [i j]
            (cond (and (< 0 i) (< 0 j) (= (nth s (dec i)) (nth t (dec j))))
                  (conj (lcs-diff-rec (dec i) (dec j)) {:keep (list (vector (dec i) (dec j)))})
                  (and (< 0 j) (or (= i 0) (<= (aget m (dec i) j) (aget m i (dec j)))))
                  (conj (lcs-diff-rec i (dec j)) {:insert (list (dec j))})
                  (and (< 0 i) (or (= j 0) (>  (aget m (dec i) j) (aget m i (dec j)))))
                  (conj (lcs-diff-rec (dec i) j) {:remove (list (dec i))})
                  :else []))]
    (f (count s) (count t))))


(defn seq-diff
  [s t]
  (let [part (partition-by #(nil? (:keep %)) (lcs-diff s t))]
    (filter (complement :keep) (map #(apply merge-with concat %) part))))

(defn merge-seq-nodes
  [diff idx]
  (let [in (:insert idx)
        rm (:remove idx)]
    (letfn [(first-key [x] (first (keys x)))
            (get-index [x]
              (cond
                (:insert x) (nth in (first (:insert x)))
                (:remove x) (nth rm (first (:remove x)))
                (:keep x)   [(nth rm (first (first (:keep x))))
                             (nth in (second (first (:keep x))))]))]
      (reduce #(merge-with conj %1 {(first-key %2) (get-index %2) })
        {:insert '() :remove '() :keep '()} diff))))

(defn find-changes
  [s t diff]
  (if (and (contains? diff :remove) (contains? diff :insert))
    (letfn [(rem-values [tree node] (dissoc (nth tree node) :value))]
      (merge-seq-nodes
        (lcs-diff
          (map (partial rem-values s) (:remove diff))
          (map (partial rem-values t) (:insert diff)))
        diff))
    diff))

(defn tree-diff
  [s t]
  (cond
   (= s t) nil
   (empty? s) {:insert (take (count t) (iterate inc 0))}
   (empty? t) {:remove (take (count s) (iterate inc 0))}
   :else (let [tdiff (map (partial find-changes s t) (seq-diff s t))
               diff (apply merge-with concat tdiff)]
           (reduce #(assoc-in %1 [%2] (sort (%2 diff))) {} (keys diff)))))

(defn simple-diff
  [s t idxs idxt]
  {:keep (filter identity (map #(if (not= %1 %2) [%3 %4]) s t
                               (iterate inc idxs) (iterate inc idxt)))})

(defn complex-diff
  [s t s-noval t-noval]
  (let [top    (count (take-while true? (map = s-noval t-noval)))
        bottom (count (take-while true? (map =
                                             (reverse s-noval)
                                             (reverse t-noval))))
        bottom-s (- (count s) bottom)
        bottom-t (- (count t) bottom)
        top-diff    (simple-diff (take top s) (take top t) 0 0)
        bottom-diff (simple-diff (drop bottom-s s) (drop bottom-t t)
                                 bottom-s bottom-t)
        rest-diff   (tree-diff (take (- bottom-s top) (drop top s))
                               (take (- bottom-t top) (drop top t)))
        ]
    (letfn [(adjust-idx
              [[key val]]
              (cond
               (= key :keep) {key (map (fn [[x y]] [(+ top x) (+ top y)]) val)}
               :else {key (map (partial + top) val)}))]
      (merge-with concat
                  (apply merge (map adjust-idx rest-diff))
                  top-diff bottom-diff))))


(defn find-diff-strategy
  [s t]
  (cond
    (= s t) nil
    (nil? s) {:insert (take (count t) (iterate inc 0))}
    (nil? t) {:remove (take (count s) (iterate inc 0))}
    :else
      (letfn [(no-vals [v] (map #(dissoc % :value) v))]
        (let [s-noval (no-vals s)
              t-noval (no-vals t)]
          (if (= s-noval t-noval)
            (simple-diff s t 0 0)
            (complex-diff s t s-noval t-noval))))))

(comment

  (defn print-subtree
    [subtree prefix]
    (if (empty? subtree)
      ""
      (apply str prefix (:tag (first subtree)) "--" (:value (first subtree)) "\n"
             (map #(print-subtree %1 (str "\t" prefix)) (rest subtree)))))

  (defn print-tree
    [tree]
    (println (print-subtree tree "")))

  (defn print-flat
    [postwalk]
    (println
     (apply str (map #(str %2 " - " %1 "\n") postwalk (iterate inc 0)))))

)
