(ns hello-clojure.chapter-3)

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


; stopped on "Hobbit Violence"
; https://www.braveclojure.com/do-things/


(str 1 2 3)                                                 ; "123"
(vector 1 2 3)                                              ; [1 2 3]
(list 1 2 3)                                                ; (1 2 3)
(hash-map :a 1 :b 2)                                        ; {:a 1, :b 2}
{:a 1 :b 2}                                                 ; {:a 1, :b 2}
(hash-set 1 1 2 3)                                          ; #{1 3 2}


(defn add-100
  [number]
  (+ number 100))

(add-100 5)                                                 ; 105

(defn inc-maker
  "Create a custom incrementor"
  [inc-by]
  #(+ % inc-by))

(defn dec-maker
  "Create a custom decrementor"
  [dec-by]
  #(- % dec-by))

(def dec9 (dec-maker 9))                                    ; defines dec9

(dec9 10)                                                   ; 1

;ex 4
(defn mapset
  [function input]
  (apply hash-set (map function input)))                    ; defines mapset

(mapset inc [1 1 2 2])                                      ; #{3 2}

;ex 5
(defn matching-parts
  [part]
  [{:name (clojure.string/replace (:name part) #"^left-" "right-")
    :size (:size part)}
   {:name (clojure.string/replace (:name part) #"^left-" "center-")
    :size (:size part)}
   {:name (clojure.string/replace (:name part) #"^left-" "up-")
    :size (:size part)}
   {:name (clojure.string/replace (:name part) #"^left-" "down-")
    :size (:size part)}])


(defn symmetrize-body-parts
  "Expects a seq of maps that have a :name and :size"
  [asym-body-parts]
  (loop [remaining-asym-parts asym-body-parts
         final-body-parts []]
    (if (empty? remaining-asym-parts)
      final-body-parts
      (let [[part & remaining] remaining-asym-parts]
        (let [[first-add-part second-add-part third-add-part fourth-add-part] (matching-parts part)]
          (recur remaining
                 (into final-body-parts
                       (set [part first-add-part second-add-part third-add-part fourth-add-part]))))))))


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


