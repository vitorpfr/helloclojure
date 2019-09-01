(ns helloclojure.hackerrank.interviewprep)

; Complete the sockMerchant function below.
; https://www.hackerrank.com/challenges/sock-merchant/problem?h_l=interview&playlist_slugs%5B%5D=interview-preparation-kit&playlist_slugs%5B%5D=warmup
(defn sockMerchant [n ar]
  (->> (map str ar)
       (map keyword)
       (frequencies)
       (into [])
       (reduce
         (fn [count-map n]
           (let [sock-count (nth n 1)
                 pair-count (quot sock-count 2)]
             (if (> sock-count 1)
               (update count-map :paircount + pair-count)
               count-map)))
         {:paircount 0})
       (:paircount))

  )


; Counting Valleys
; Complete the countingValleys function below.
; https://www.hackerrank.com/challenges/counting-valleys/problem?h_l=interview&playlist_slugs%5B%5D=interview-preparation-kit&playlist_slugs%5B%5D=warmup
(defn countingValleys [n s]
  (->> s
       (into [])
       (map int)
       (reduce
         (fn [hike-map current-step]
           (cond
             (and (= current-step 85) (= (:current-level hike-map) -1))
             (-> hike-map
                 (update ,,,, :current-level inc)
                 (update ,,,, :valley-count inc))
             (= current-step 85)                                   ; 85 equals to letter "U"
             (update hike-map :current-level inc)
             (= current-step 68)
             (update hike-map :current-level dec)                  ; 68 equals to letter "D"
             :else
             hike-map))
         {:current-level 0
          :valley-count 0})
       (:valley-count))
  )


