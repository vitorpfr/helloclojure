(ns hello-clojure.codewars)

(ns accumule.core
  (:use [clojure.string :only [join capitalize]]))

(defn repeat-capitalize [i elm]
  (capitalize (apply str (repeat (inc i) elm))))

(defn accum [s]
  (join "-" (map-indexed repeat-capitalize s)))

; mumbling challenge - check solution later
; https://www.codewars.com/kata/mumbling/solutions?show-solutions=1

(ns jaden-case
  (:require [clojure.string :as str]))

(defn jaden-case [s]
  (->> (str/split (str s) #"\b")
       (map str/capitalize)
       str/join))



sum + x / (+ (count arr) 1) = avg

sum + x = (* avg (+ (count arr) 1))

x = (- (* avg (+ (count arr) 1)) sum)


(defn new-avg [arr navg]
  (let [sum (reduce + arr)]
    (try
      (- (* navg (+ (count arr) 1)) sum)
      (catch Exception nil)
      )))

(defn new-avg [arr navg]
  (let [sum (reduce + arr)]
    (let [result (- (* navg (+ (count arr) 1)) sum)]
      (if (< result 0)
        nil
        result)
      )))


(defn new-avg [arr navg]
  (let [sum (reduce + arr)]
    (let [result (- (* navg (+ (count arr) 1)) sum)]
      (if (= result nil)
        (throw IllegalArgumentException.)
        result)
      )))

