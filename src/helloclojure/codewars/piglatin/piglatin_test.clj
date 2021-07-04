(ns helloclojure.codewars.piglatin.piglatin-test
  (:require [clojure.test :refer :all]
            [helloclojure.codewars.piglatin.piglatin :refer :all]))

(deftest pig-latin-example-test
  (is (= (pig-it "Pig latin is cool") "igPay atinlay siay oolcay"))
  (is (= (pig-it "This is my string") "hisTay siay ymay tringsay")))
