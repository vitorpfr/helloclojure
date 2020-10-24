(ns helloclojure.brave.chapter-3)

; main function
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Print me on terminal!"))

; example of a function that can receive 1 or 2 parameters
(defn x-chop
  "Describe the kind of chop you're inflicting on someone"
  ([name chop-type]
   (str "I " chop-type " chop " name "! Take that!"))
  ([name]
   (x-chop name "karate")))


(def asym-hobbit-body-parts [{:name "head" :size 3}
                             {:name "left-eye" :size 1}
                             {:name "left-ear" :size 1}
                             {:name "mouth" :size 1}
                             {:name "nose" :size 1}
                             {:name "neck" :size 2}
                             {:name "left-shoulder" :size 3}
                             {:name "left-upper-arm" :size 3}
                             {:name "chest" :size 10}
                             {:name "back" :size 10}
                             {:name "left-forearm" :size 3}
                             {:name "abdomen" :size 6}
                             {:name "left-kidney" :size 1}
                             {:name "left-hand" :size 2}
                             {:name "left-knee" :size 2}
                             {:name "left-thigh" :size 4}
                             {:name "left-lower-leg" :size 3}
                             {:name "left-achilles" :size 1}
                             {:name "left-foot" :size 2}])

; example of loop
(loop [iteration 0
       my-accumulator {}]
  (println (str "Accumulator " my-accumulator))
  (if (> iteration 3)
    (println "Goodbye!")
    (recur (inc iteration)
           (assoc my-accumulator iteration "oi"))))

; my implementation of reduce
(defn my-reduce
  ([f initial coll]
   (loop [result initial
          remaining coll]
     (if (empty? remaining)
       result
       (recur (f result (first remaining))
              (rest remaining)))))
  ([f [head & tail]]
   (my-reduce f head tail)))


(def asym-hobbit-body-parts [{:name "head" :size 3}
                             {:name "left-eye" :size 1}
                             {:name "left-ear" :size 1}
                             {:name "mouth" :size 1}
                             {:name "nose" :size 1}
                             {:name "neck" :size 2}
                             {:name "left-shoulder" :size 3}
                             {:name "left-upper-arm" :size 3}
                             {:name "chest" :size 10}
                             {:name "back" :size 10}
                             {:name "left-forearm" :size 3}
                             {:name "abdomen" :size 6}
                             {:name "left-kidney" :size 1}
                             {:name "left-hand" :size 2}
                             {:name "left-knee" :size 2}
                             {:name "left-thigh" :size 4}
                             {:name "left-lower-leg" :size 3}
                             {:name "left-achilles" :size 1}
                             {:name "left-foot" :size 2}])

; exercises

; 1
(str "oi " "moço")
(vector 1 2 3 4)
(list 1 2 3 4)
(hash-map :a 1 :b 2)
(hash-set 1 1 2 3 4 4 4)

; 2
(defn add-100 [x] (+ 100 x))

; 3
(defn dec-maker [dec]
  (fn [n] (- n dec)))
; testing
(def dec9 (dec-maker 9))
(dec9 10)

; 4
(defn mapset [f coll]
  (into #{} (map f coll)))

(mapset inc [1 1 2 2])

; 5
(def asym-parts [{:name "head" :size 3}
                 {:name "left-eye" :size 1}
                 {:name "left-ear" :size 2}
                 {:name "mouth" :size 1}
                 {:name "nose" :size 1}])

;(defn old-matching-part
;  [{:keys [name size]}]
;  {:name (clojure.string/replace name #"^left-" "right-")
;   :size size})

(defn matching-part [part]
  (update part :name clojure.string/replace #"^left-" "right-"))

; previous version
;(defn symmetrize-body-parts
;  "Expects a seq of maps that have a :name and :size"
;  [asym-body-parts]
;  (reduce (fn [final-body-parts part]
;            (into final-body-parts (set [part (matching-part part)])))
;          []
;          asym-body-parts))

(def part-prefixes ["left-"
                    "right-"
                    "top-"
                    "bottom-"
                    "center-"])

; result
(defn symmetrize-body-parts
  "Expects a seq of maps that have a :name and :size"
  [asym-body-parts]
  (reduce (fn [final-body-parts part]
            (if (re-find #"^left-" (:name part))
              (into final-body-parts (map #(update part :name clojure.string/replace #"^left-" %)
                                          part-prefixes))
              (into final-body-parts [part])))
          []
          asym-body-parts))

(symmetrize-body-parts asym-parts)

(re-find #"^left-" "left-ear")

; 6
(def asym-parts-2 [{:name "head" :size 3}
                   {:name "eye-1" :size 1}
                   {:name "ear-1" :size 2}
                   {:name "mouth" :size 1}
                   {:name "nose" :size 1}])

(re-find #"-1$" "eye-1")
(re-find #"-1$" "head")

; result
(defn symmetrize-any-body-parts
  [asym-body-parts n]
  (let [symmetrical-parts-list (->> (range n)
                                    (map inc)
                                    (map #(str "-" %)))]
    (reduce (fn [final-body-parts part]
              (if (re-find #"-1$" (:name part))
                (into final-body-parts (map #(update part :name clojure.string/replace #"-1$" %)
                                            symmetrical-parts-list))
                (into final-body-parts [part])))
            []
            asym-body-parts)))

(symmetrize-any-body-parts asym-parts-2 15)