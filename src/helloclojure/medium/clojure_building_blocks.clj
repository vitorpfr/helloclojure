(ns helloclojure.medium.clojure-building-blocks
  (:require [clojure.string :as string]))

; exercises from:
; https://medium.com/@daniel.oliver.king/getting-work-done-in-clojure-the-building-blocks-39ad82796926

(defn total-of-positives
  "Gets the sum of a sequence of numbers. Non-positive numbers
  should not be included in the sum.
  Example:
  (total-of-positives [1 5 -10 3 -2])
  -> 9"
  [nums]
  (->> nums
       (filter pos?)
       (reduce + 0)))

; another approach
(defn total-of-positives-alt
  "Gets the sum of a sequence of numbers. Non-positive numbers
  should not be included in the sum.
  Example:
  (total-of-positives [1 5 -10 3 -2])
  -> 9"
  [nums]
  (reduce (fn [acc x] (if (pos? x) (+ acc x) acc))
          0
          nums))

; Testing fn
(println (= 9 (total-of-positives [1 5 -10 3 -2])))
(println (= 9 (total-of-positives-alt [1 5 -10 3 -2])))



(defn is-palindrome
  "Determines whether or not a given string is a palindrome.
  Examples:
  (is-palindrome \"Hello\") -> false
  (is-palindrome \"abcba\") -> true
  Challenge: (is-palindrome \"Taco Cat\") -> true"
  [str]
  (let [clean-str (-> str
                      string/lower-case
                      (string/replace #" " ""))]
    (= clean-str (string/reverse clean-str))))

; Testing fn
(println (true? (is-palindrome "abcba")))
(println (false? (is-palindrome "Hello")))
(println (true? (is-palindrome "Taco Cat")))

(def start-lowercase (int \a))
(def end-lowercase (int \z))
(def end-uppercase (int \Z))
(def alphabet-size (inc (- end-lowercase start-lowercase)))

(defn- rotate-letter-forward
  [letter num-places]
  (let [letter-position                 (int letter)
        theoretical-new-letter-position (+ letter-position num-places)
        new-letter-position             (cond
                                          (Character/isUpperCase letter)
                                          (if (> theoretical-new-letter-position end-uppercase)
                                            (- theoretical-new-letter-position alphabet-size)
                                            theoretical-new-letter-position)

                                          (Character/isLowerCase letter)
                                          (if (> theoretical-new-letter-position end-lowercase)
                                            (- theoretical-new-letter-position alphabet-size)
                                            theoretical-new-letter-position)

                                          :else
                                          (throw (Exception. "word provided is not valid")))]
    (char new-letter-position)))

(defn caesar-encrypt
  "Takes a word and a number, and rotates each letter in the word
  that many characters forward in the alphabet, wrapping around from
  Z back to A.
  Examples:
  (caesar-encrypt \"abc\" 3) -> \"def\"
  (caesar-encrypt \"Zebra\" 4) -> \"Difve\"
  Note: The name of the function comes from the fact that this transformation
  is known as a Caesar Cipher."
  [word num-places]
  (->> (vec word)
       (reduce (fn [acc letter]
                 (conj acc (rotate-letter-forward letter num-places)))
               [])
       string/join)
  )

(caesar-encrypt "Zebra" 4)

; Testing fn
(println (= "def" (caesar-encrypt "abc" 3)))
(println (= "Difve" (caesar-encrypt "Zebra" 4)))
