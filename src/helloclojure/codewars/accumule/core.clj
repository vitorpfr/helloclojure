(ns helloclojure.codewars.accumule.core
  (:require [clojure.string :as str]))

; https://www.codewars.com/kata/mumbling

(defn repeat-capitalize [i elm]
  (str/capitalize (apply str (repeat (inc i) elm))))

(defn accum [s]
  (->> s
       (map-indexed repeat-capitalize)
       (str/join "-")))
