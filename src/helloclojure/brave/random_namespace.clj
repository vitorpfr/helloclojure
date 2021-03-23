(ns helloclojure.brave.random-namespace
  (:require [helloclojure.brave.chapter-13 :as were-creatures]))

(defmethod were-creatures/full-moon-behavior :bill-murray
  [were-creature]
  (str (:name were-creature) " will be the most likeable celebrity"))

(were-creatures/full-moon-behavior {:name "Laura the intern"
                                    :were-type :bill-murray})
