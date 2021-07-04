(ns helloclojure.codewars.piglatin.piglatin
  (:require [clojure.string :as str]))

(defn split-words
  [s]
  (str/split s #" "))

(defn move-first-letter-to-end
  [s]
  (let [first-letter (list (first s))
        rest-of-word (rest s)]
    (-> (concat rest-of-word first-letter)
        str/join)))

(defn only-letters? [s]
  (every? #(Character/isLetter ^char %) s))

(defn add-ay-to-end [s]
  (if (only-letters? s)
    (str s "ay")
    s))

(defn rejoin-words [st-list]
  (str/join " " st-list))

(defn pig-it [s]
  (->> s
       split-words
       (map move-first-letter-to-end)
       (map add-ay-to-end)
       rejoin-words))


; tests
(def input-1 "list Bacon ham")
(split-words input-1)

(def input-2 "list")
(move-first-letter-to-end input-2)

(def input-3 ["list" "bacon" "ham"])
(map move-first-letter-to-end input-3)

(def input-4 ["istl" "aconB" "amh" "!"])
(map add-ay-to-end input-4)

(def input-5 "aaa")
(only-letters? input-5)

(def input-6 "!")
(only-letters? input-6)

(pig-it input-1)


