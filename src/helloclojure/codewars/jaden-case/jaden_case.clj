(ns helloclojure.codewars.jaden-case
  (:require [clojure.string :as str]))

(defn jaden-case [s]
  (->> (str/split (str s) #"\b")
       (map str/capitalize)
       str/join))