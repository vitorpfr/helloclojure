(ns helloclojure.brave.chapter-5)

; using recursion to build a sum (which would be done with side-effects on
; other languages

(defn sum
  ([vals] (sum vals 0))
  ([vals accumulating-total]
   (if (empty? vals)
     accumulating-total
     (sum (rest vals) (+ (first vals) accumulating-total)))))

(sum [1 2 3 4])

; better to do it using recur instead of recursion for tail-call optimization
(defn sum
  ([vals] (sum vals 0))
  ([vals accumulating-total]
   (if (empty? vals)
     accumulating-total
     (recur (rest vals) (+ (first vals) accumulating-total)))))

(sum [1 2 3 4])

((comp inc *) 2 3)
; same as
(inc (* 2 3))

(defn two-comp [f g]
  (fn [& args]
    (f (apply g args))))

((two-comp inc *) 2 3)

; comp for multiple functions, using reduce
(defn my-comp [& fns]
  (fn [& args]
    (reduce (fn [result-so-far next-fn] (next-fn result-so-far))
            (apply (last fns) args)
            (rest (reverse fns)))))

; comp for multiple functions, using reduce and let
(defn my-comp [& fns]
  (fn [& args]
    (let [ordered-fns (reverse fns)
          first-result (apply (first ordered-fns) args)]
      (reduce (fn [result-so-far next-fn] (next-fn result-so-far))
              first-result
              (rest ordered-fns)))))

((my-comp inc *) 2 3)

; comp for multiple functions, using loop instead of reduce
(defn my-comp-two [& fns]
  (fn [& args]
    (let [ordered-fns (reverse fns)
          first-result (apply (first ordered-fns) args)]
      (loop [result-so-far first-result
             remaining-fns (rest ordered-fns)]
        (if (empty? remaining-fns)
          result-so-far
          (recur ((first remaining-fns) result-so-far)
                 (rest remaining-fns)))))))

((my-comp-two inc *) 2 3)

; memoize: remembers the result of a function call
; useful for functions that take a lot of time to run
(defn sleepy-identity [x]
  (Thread/sleep 1000)
  x)

; takes 1 sec
(sleepy-identity "Testing")

(def memo-sleepy-identity (memoize sleepy-identity))

; Takes 1 sec
(memo-sleepy-identity "Testing")

; runs immediately after the first time
(memo-sleepy-identity "Testing")
(memo-sleepy-identity "Testing")

; peg thing
(defn tri*
  "Generates lazy sequence of triangular numbers"
  ([] (tri* 0 1))
  ([sum n]
   (let [new-sum (+ sum n)]
     (cons new-sum (lazy-seq (tri* new-sum (inc n)))))))

(def tri (tri*))

(take 5 tri)

(defn triangular?
  "Is the number triangular? e.g. 1, 3, 6, 10, 15, etc" [n]
  (= n (last (take-while #(>= n %) tri))))

; exercises

; 1
; You used (comp :intelligence :attributes) to create a function that
; returns a character’s intelligence. Create a new function, attr,
; that you can call like (attr :intelligence) and that does the same thing.

(def character {:name       "Smooches McCutes"
                :attributes {:intelligence 10
                             :strength     4
                             :dexterity    5}})

((comp :intelligence :attributes) character)

(defn attr [attribute]
  (comp attribute :attributes))

; testing
((attr :intelligence) character)

; 2
; Implement the comp function.
((comp :intelligence :attributes) character)

(defn my-comp-ex [& fns]
  (fn [& args]
    (let [ordered-fns (reverse fns)]
      (reduce (fn [result-so-far next-fn]
                (next-fn result-so-far))
              (apply (first ordered-fns) args)
              (rest ordered-fns)))))

((my-comp-ex :intelligence :attributes) character)

; 3
;Implement the assoc-in function.
; Hint: use the assoc function and define its parameters as [m [k & ks] v].
(assoc-in {} [:cookie :monster :vocals] "Finntroll")
(assoc-in {:cookie {:other-key 5}}
          [:cookie :monster :vocals]
          "Finntroll")

(defn my-assoc-in
  [m [k & ks] v]
  (if ks
    (assoc m k (my-assoc-in (get m k) ks v))
    (assoc m k v)))

(my-assoc-in {} [:cookie :monster :vocals] "Finntroll")

; first call
(assoc {} :cookie (my-assoc-in (get {} :cookie) [:monster :vocals] "Finntroll"))
; second call
(assoc {} :cookie (assoc {} :monster (my-assoc-in (get {} :monster) [:vocals] "Finntroll")))
; third call
(assoc {} :cookie (assoc {} :monster (assoc {} :vocals "Finntroll")))

; 4
; Look up and use the update-in function.
(let [my-map {:cookie {:monster {:vocals 5}}}]
  (update-in my-map [:cookie :monster :vocals] inc))

(let [my-map {:cookie {:monster {:vocals 5}}}]
  (update-in my-map [:cookie :other :stuff] str))

; 5
; Implement update-in.
(let [my-map {:cookie {:monster {:vocals 5}}}]
  (update-in my-map [:cookie :monster :vocals] + 9))

; my-update-in for functions without args
(defn my-update-in
  [m [k & ks] f]
  (if ks
    (assoc m k (my-update-in (get m k) ks f))
    (update m k f)))

; testing
(let [my-map {:cookie {:monster {:vocals 5}}}]
  (my-update-in my-map [:cookies :monst] str))

(let [my-map {:cookie {:monster {:vocals 5}}}]
  (my-update-in my-map [:cookie :monster :vocals] inc))

; my-update-in for functions with args
(defn my-update-in-w-args
  [m [k & ks] f & args]
  (if ks
    (assoc m k (apply my-update-in-w-args (get m k) ks f args))
    (apply update m k f args)))

; testing
(let [my-map {:cookie {:monster {:vocals 5}}}]
  (my-update-in-w-args my-map [:cookie :monster :vocals] + 9))

(let [my-map {:cookie {:monster {:vocals 5}}}]
  (my-update-in-w-args my-map [:cookie :monster :vocals] str " oi"))