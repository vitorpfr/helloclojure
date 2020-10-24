(ns helloclojure.brave.chapter-4)

(defn titleize
  [topic]
  (str topic " for the Brave and True"))

(map titleize ["Hamsters" "Ragnarok"])
(map titleize '("Empathy" "Decorating"))
(map titleize #{"Elbows" "Soap Carving"})
(map #(titleize (second %)) {:uncomfortable-thing "Winking"})

; trying to implement map myself
; with recur - not working
(defn my-map
  [f col]
  (loop [[head & tail] col
         result '()]
    (if (empty? tail)
      (cons (f head) result)
      (recur tail
             (cons (f head) result)))))

; with recursion - works
(defn my-map-two
  [f [head & tail]]
  (if (empty? tail)
    [(f head)]
    (cons (f head) (my-map-two f tail))))

first

(map inc [1 2 3])
(my-map inc [1 2 3])

(my-map-two inc [1 2 3])

; seq of a map is a list of vectors
(seq {:a 1 :b 2})

; we can convert it back to a map using into
(into {} (seq {:a 1 :b 2}))

; map on two collections
(def human-consumption [8.1 7.3 6.6 5.0])
(def critter-consumption [0.0 0.2 0.3 1.1])

(defn unify-diet-data
  [human critter]
  {:human   human
   :critter critter})

(map unify-diet-data human-consumption critter-consumption)

; map receiving a list of functions
(def sum #(reduce + %))
(def avg #(/ (sum %) (count %)))
(defn stats
  [numbers]
  (zipmap [:sum :count :avg]
          (map #(% numbers) [sum count avg])))

(stats [3 4 10])

; use reduce to update a map values
(defn update-map-vals [m f]
  (reduce (fn [new-map [key val]]
            (assoc new-map key (f val)))
          {}
          m))

(update-map-vals {:min 10
                  :max 30}
                 inc)

; use reduce to filter out keys on a map
(reduce (fn [new-map [key val]]
          (if (> val 4)
            (assoc new-map key val)
            new-map))
        {}
        {:human   4.1
         :critter 3.9})

; implement map using reduce
(defn my-map-using-reduce
  [f coll]
  (reduce (fn [new-seq element]
            (conj new-seq (f element)))
          []
          coll))

(my-map-using-reduce inc '(1 2 3))

; implement filter using reduce
(defn my-filter-using-reduce
  [f coll]
  (reduce (fn [new-seq element]
            (if (f element)
              (conj new-seq element)
              new-seq))
          []
          coll))

(my-filter-using-reduce odd? [1 2 3 4 5 6 7])

; implement some using reduce
(defn my-some-using-reduce
  [f coll]
  (reduce (fn [_ element]
            (when (f element)
              (reduced (f element))))
          nil
          coll))

(my-some-using-reduce even? '(1 4 3 5))


; take-while and filter difference
(take-while neg? [-2 -1 0 1 2 3])
(filter neg? [-2 -1 0 1 2 3])
; they return the same result in this case, but filter processes the whole
; seq, while take-while stops in the first false result
; take-while is more efficient if the seq is already ordered


; lazy seq

; this is our database of suspect vampires
(def vampire-database
  {0 {:makes-blood-puns? false, :has-pulse? true :name "McFishwich"}
   1 {:makes-blood-puns? false, :has-pulse? true :name "McMackson"}
   2 {:makes-blood-puns? true, :has-pulse? false :name "Damon Salvatore"}
   3 {:makes-blood-puns? true, :has-pulse? true :name "Mickey Mouse"}})

; operation to look up an entry in the database takes 1 sec
(defn vampire-related-details [social-security-number]
  (Thread/sleep 1000)
  (get vampire-database social-security-number))

; returns a record if it passes the vampire test
(defn vampire?
  [record]
  (and (:makes-blood-puns? record) (not (:has-pulse? record)) record))

; maps social security numbers to database records and returns the
; first record that indicates vampirism
(defn identify-vampire
  [social-security-numbers]
  (first (filter vampire?
                 (map vampire-related-details social-security-numbers))))

; checking that it takes 1 second to consult a SSN
(time (vampire-related-details 0))

; therefore, it would take 1 million secs to consult 1 million registers?
; if we do non-lazy, yes

; because map is lazy, it doesn't actually apply vampire-related-details
; to all the 1 million elements until we actually access any of them
; therefore this takes less than 1 sec
(time (def mapped-details (map vampire-related-details (range 0 1000000))))

; lazy seqs create a "recipe" on how to perform a calculation, but doesn't
; actually do it until you try to access one of the results

; in the previous example, mapped-details is unrealized.
; once we try to access a member of mapped-details, it will use
; its recipe to generate the element you’ve requested, and we’ll
; incur the one-second-per-database-lookup cost:
(time (first mapped-details))

; why does it take 32 seconds, and not 1 sec (since it is acessing the first element)?
; answer: clojure chunks its lazy seqs, performing 32 accesses instead of one
; this results in better performance in general

; lazy seqs need to be realized only once, if I try again it is very fast
(time (first mapped-details))

; so, finding the vampire in 1 million registers take 32 seconds instead
; of the following:
; we don't need to access all registers in the map since the lazy seq is a recipe (so it doesn't take 1 million secs)
; however, it doesn't take 3 seconds (register is the third) because clojure work in chunks and prepares the first 32 registers at once
(time (identify-vampire (range 0 1000000)))

; infinite lazy sequences using repeat and repeatedly
(concat (take 8 (repeat "na")))
(take 3 (repeatedly (fn [] (rand-int 10))))


; building my own inifite lazy seq
(defn even-numbers
  ([] (even-numbers 0))
  ([n] (cons n (lazy-seq (even-numbers (+ n 2))))))
(take 10 (even-numbers))

(defn even-numbers
  ([] (even-numbers 0))
  ([n] (cons n (lazy-seq (even-numbers (+ n 2))))))


; map converts anything to seq
(map identity [:garlic :sesame-oil :fried-eggs])
; use into to get it back to the original data structure
(into [] (map identity [:garlic :sesame-oil :fried-eggs]))

; conj in terms of into
(defn my-conj
  [target & additions] (into target additions))
; two functions that do the same thing, except
; one takes a rest parameter (conj) and one takes a seqable data structure (into).

; apply: explodes the arguments
(apply max [0 1 2])
; is the same as
(max 0 1 2)


; (partial) function
; defining partial in terms of apply and into
(defn my-partial
  [partial-fn & args]
  (fn [& more-args]
    (apply partial-fn (into args more-args))))
; into is used to get args together (previous and new)
; apply explodes the args list and pass it to the function


((my-partial + 5) 3)

(defn lousy-logger
  [log-level message]
  (condp = log-level
    :warn (clojure.string/lower-case message)
    :emergency (clojure.string/upper-case message)))

(def warn (partial lousy-logger :warn))

(warn "Red light ahead")
; => "red light ahead"

; complement: negation of a boolean function
; vampire? returns if record is a vampire

#(not (vampire? %))
; this could be
(complement vampire?)

; complement in terms of apply
(defn my-complement
  [f]
  (fn [& args]
    (not (apply f args))))
; apply is used to explode args to pass to f

; exercises done on chapter_4_fwpd.clj

