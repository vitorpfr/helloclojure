(ns hello-clojure.hackerrank.exercises)

; For loop example that works
(for [i (range 3)] [i i])

; Compare the Triplets
; Check how in how many indexes a>b and b>a
(defn compareTriplets [a b]
  [(count (filter #{true} (map > a b))) (count (filter #{true} (map < a b)))]
  )

; Diagonal difference (matrix n x n)
; Gets absolute difference of sum of two diagonals of a matriz
(defn diagonalDifference [arr n]

  (def primary-coord      (for [i (range n)] [i i]))
  (def secondary-coord    (for [i (range n)] [i (- n 1 i)]))

  (let
    [first-diag (for [i (range n)] (get-in arr (nth primary-coord i)))
     second-diag (for [i (range n)] (get-in arr (nth secondary-coord i)))
     ]
    (Math/abs
      (-
        (reduce + first-diag)
        (reduce + second-diag)))

    )


  )

; Plus minus (n is size of array)
; Given an array, prints the percentage of positive, negative and zero values
(defn plusMinus [arr n]

  (let
    [len (* 1.0 n)
     zeros (vec (replicate n 0))
     plus (count (filter #{true} (map > arr zeros)))
     zero (count (filter #{true} (map = arr zeros)))
     minus (count (filter #{true} (map < arr zeros)))]
    (prn (float (/ plus len)))
    (prn (float (/ minus len)))
    (prn (float (/ zero len)))
    )

  )

; Staircase
; Print a staircase of size n
(defn staircase [n]
  (doseq [i (range n)]
    (print (apply str (repeat (- n i 1) " ")))
    (print (apply str (repeat (+ i 1) "#")))
    (newline)
    )
  )

; Mini-max sum
; Given an array of 5 elements, return the lowest and highest sum of 4
(defn miniMaxSum [arr]
  (let [sorted-arr (sort arr)]
    (print (reduce + (take 4 sorted-arr)))
    (print " ")
    (print (reduce + (take-last 4 sorted-arr)))
    )
  )


; Birthday cake candles
(defn birthdayCakeCandles [ar]
  (let [max-value (apply max ar)]
    (count (filter #{max-value} ar))
    )
  )

; Time conversion: convert 12h (12:40:22AM) to 24h (00:40:22)
(require '[clojure.string :as str])
;
; Complete the timeConversion function below.
;
(defn timeConversion [s]
  (let [parsed-time
        (str/split (subs s 0 8) #":")
        int-hour
        (Integer/parseInt (first parsed-time))
        new-parsed-time
        (cond
          (and (str/ends-with? s "AM") (= int-hour 12))
          (assoc parsed-time 0 "00")
          (and (str/ends-with? s "PM") (> int-hour 0) (< int-hour 12))
          (assoc parsed-time 0 (str (+ int-hour 12)))
          :else
          parsed-time)]

    (str/join ":" new-parsed-time)
    )

  )


; Grading students
(defn gradingStudents [grades]

  (let [rounder
        (fn [n]
          (if (or (< n 38) (< (rem n 5) 3))
            n
            (- (+ n 5) (rem n 5))))]

    (map rounder grades))

  )


; Apple and orange

; Complete the countApplesAndOranges function below.
(defn countApplesAndOranges [s t a b apples oranges]
  ; calculate position of apples and oranges
  (let [applePosition (map (fn [n] (+ a n)) apples)
        orangePosition (map (fn [n] (+ b n)) oranges)
        fellInHouse (fn [n] (and (>= n s) (<= n t)))
        appleFellInHouse (map fellInHouse applePosition)
        orangeFellInHouse (map fellInHouse orangePosition)]
    (println (count (filter #{true} appleFellInHouse)))
    (println (count (filter #{true} orangeFellInHouse)))
    )

  )

; Between Two Sets
; You will be given two arrays of integers and asked to determine all integers that satisfy the following two conditions:
;
;The elements of the first array are all factors of the integer being considered
;The integer being considered is a factor of all elements of the second array

(defn gcd
      [a b]
      (if (zero? b)
        a
        (recur b, (mod a b))))

(defn gcdv [& v] (reduce gcd v))

(defn lcm
      [a b]
      (/ (* a b) (gcd a b)))

(defn lcmv [& v] (reduce lcm v))

(defn factors [n]
      (filter #(zero? (rem n %)) (range 1 (inc n))))

(defn getTotalX [a b]
      (let [x (apply lcmv a)
            y (apply gcdv b)
            z (apply vector (range x (inc y) x))
            w (vec (replicate (count z) y))]
           ;;Count the number of multiples of LCM that evenly divides the GCD
           (count (filter #{0} (map rem w z)))
           )
      )



;; Breaking records

;function to be used on reduce
(cond
  (> x (:max record-data))
  (-> record-data
      (update :maxcount inc)
      (assoc :max n))
  (< x (:min record-data))
  (-> record-data
      (update :mincount inc)
      (assoc :min n))
  :else record-data)


; final result
(let [eval-score (fn [record-data x]
                   (cond
                     (> x (:max record-data))
                     (-> record-data
                         (update :maxcount inc)
                         (assoc :max x))
                     (< x (:min record-data))
                     (-> record-data
                         (update :mincount inc)
                         (assoc :min x))
                     :else record-data))
      eval-result (reduce
                    eval-score
                    {:min (first scores)
                     :max (first scores)
                     :mincount 0
                     :maxcount 0}
                    (rest scores))]
  (str
    (:maxcount eval-result)
    " "
    (:mincount eval-result)))



;; birthday chocolate

;individual steps:
; gets one subvector
(subvec test-vector i (+ i m))

; gets all sequential subvectors of length m inside test-vector
(doseq [i (range 3)]
  (println (subvec test-vector i (+ i m))))


; transform initial sequence [1 2 3 4 5] into new one [[1 2] [2 3] [3 4] [4 5]]
(reduce
  (fn [vector i]
    (let [remaining-vector (subvec s (count vector))
          i-index (.indexOf remaining-vector i)]
      (if (< (+ m (count vector)) (inc (count s)))
        (conj vector (subvec remaining-vector i-index (+ i-index m)))
        vector)
      )
    )
  []
  s)

; transforms [[1 2][2 3]] into [3 5] (sums vectors inside vector)
(map (fn [n] (reduce + n)) test-vector-2)


; final result!
(defn birthday [s d m]
  (let [create-subsequences (fn [vector i]
                              (let [remaining-vector (subvec s (count vector))
                                    i-index (.indexOf remaining-vector i)]
                                (if (< (+ m (count vector)) (inc (count s)))
                                  (conj vector (subvec remaining-vector i-index (+ i-index m)))
                                  vector)
                                )
                              )
        subsequences-vector (reduce create-subsequences [] s)
        subsequences-sum (map (fn [n] (reduce + n)) subsequences-vector)
        sum-equal-to-birthday (count (filter #{d} subsequences-sum))]
    sum-equal-to-birthday
    )
  )
