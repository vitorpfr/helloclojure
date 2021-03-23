(ns helloclojure.brave.chapter-13
  (:import (helloclojure.brave.chapter_13 MyProtocol)))

; polymorphism: associating an operation name with more than one algorithm

;;;; multimethods

; multimethod: a direct way to introduce polymorphism into your code

; associates a name with multiple implementations through a dispatch function

; dispatching fns produce dispatching values to determine which method to use

(defmulti full-moon-behavior (fn [were-creature] (:were-type were-creature)))

(defmethod full-moon-behavior :wolf
  [were-creature]
  (str (:name were-creature) " will howl and murder"))

(defmethod full-moon-behavior :simmons
  [were-creature]
  (str (:name were-creature) " will encourage people and sweat to the oldies"))

; it is possible to have dispatch value nil
(defmethod full-moon-behavior nil
  [were-creature]
  (str (:name were-creature) " will stay at home and eat ice cream"))

; :default specifies a default method for the function
(defmethod full-moon-behavior :default
  [were-creature]
  (str (:name were-creature) " will stay up all night fantasy footballing"))

(full-moon-behavior {:were-type :wolf
                     :name      "Rachel from next door"})   ; calls method for :wolf

(full-moon-behavior {:name      "Andy the baker"
                     :were-type :simmons})                  ; calls method for :simmons

(full-moon-behavior {:were-type nil
                     :name      "Martin the nurse"})        ; calls method for nil

(full-moon-behavior {:were-type :office-worker
                     :name      "Jimmy from sales"})        ; calls method for default bc :office-worker method isnt defined

; you can add new methods on existing multimethods from other ns
; check random-namespace ns for an example

; the dispatching fn can also return arbitrary values using any or all of its args
(defmulti types (fn [x y] [(class x) (class y)]))

(defmethod types [java.lang.String java.lang.String]
  [x y]
  "Two strings!")

(types "String 1" "String 2")                               ; returns "Two strings!



;;;; protocols

; a common use of methods is to dispatch according to the type of the input
; although it is possible to do it using multimethods, protocols are optimized for this

(defprotocol Psychodynamics
  "Plumb the inner depths of your data types"

  (thoughts [x] "The data type's innermost thoughts")
  (feelings-about [x] [x y] "Feelings about self or other"))

; a protocol defines an abstraction, but I haven't defined yet how the abstraction is implemented

(thoughts "blorb")                                          ; error bc no implementation of thoughts for string

; extending protocol for string args
(extend-type java.lang.String
  Psychodynamics
  (thoughts [x] (str x " thinks, 'Truly, the character defines the data type'"))
  (feelings-about
    ([x] (str x " is longing for a simpler way of life"))
    ([x y] (str x " is envious of " y "'s simpler way of life"))))

(thoughts "blorb")
(feelings-about "schmorb")
(feelings-about "schmorb" 2)

; to provide a default implementation, i can extend java.lang.Object
(extend-type java.lang.Object
  Psychodynamics
  (thoughts [x] "Maybe the Internet is just a vector for toxoplasmosis")
  (feelings-about
    ([x] "meh")
    ([x y] (str "meh about " y))))

(thoughts 3)
(feelings-about 3)
(feelings-about 3 "blorb")
; since we didn't define a method for numbers, clojure dispatches calls for the default

; extend-protocol can be used to extend multiple types at once
(extend-protocol Psychodynamics
  java.lang.String
  (thoughts [x] "Truly, the character defines the data type")
  (feelings-about
    ([x] "longing for a simpler way of life")
    ([x y] (str "envious of " y "'s simpler way of life")))

  java.lang.Object
  (thoughts [x] "Maybe the Internet is just a vector for toxoplasmosis")
  (feelings-about
    ([x] "meh")
    ([x y] (str "meh about " y))))

;;;; records
(defrecord WereWolf [name title])
; custom map-like data types

; different forms to create an instance of the record:
(WereWolf. "David" "London Tourist")                        ; should not be used
(->WereWolf "Jacob" "Lead Shirt Discarder")
(map->WereWolf {:name "Lucian" :title "CEO of Melodrama"})

(def jacob (->WereWolf "Jacob" "Lead Shirt Discarder"))
(.name jacob)
(:name jacob)
(get jacob :name)

; any function that can be used on a map can be used on a record
(assoc jacob :title "Lead Third Wheel")

; however, if you dissoc a field, the result will be a clojure map!!!
(dissoc jacob :title)                                       ; this returns a map, not a record

; accessing map values is slower than accessing record values!
; so, it's good to use records whenever possible

; you can extend a protocol when defining a record
(defprotocol WereCreature
  (full-moon-behaviors [x]))

; the method can use the name key, which is a key of the record
(defrecord WereWolf [name title]
  WereCreature
  (full-moon-behaviors [this]
    (str name " will howl and murder")))

(let [luc (map->WereWolf {:name "Lucian" :title "CEO of Melodrama"})]
  (full-moon-behaviors luc))

; In general, you should consider using records if you find yourself creating maps
; with the same fields over and over.

; also, accessing records is more efficient than accessing simple maps


;; Exercises

;1. Extend the full-moon-behavior multimethod to add behavior for your
;own kind of were-creature.

(defmethod full-moon-behavior :worgen
  [were-creature]
  (str (:name were-creature) " comes directly from Gilneas!"))

(full-moon-behavior {:name      "Gene the worgen"
                     :were-type :worgen})

;2. Create a WereSimmons record type, and then extend the WereCreature
;protocol.
(defrecord WereSimmons
  [name title]
  WereCreature
  (full-moon-behaviors [this] (str name " is considered " title)))

(def john (->WereSimmons "John" "the slayer"))
(full-moon-behaviors john)

;3. Create your own protocol, and then extend it using extend-type and
;extend-protocol.

(defprotocol MyProtocol
  (my-type [x]))

(extend-type java.lang.String
  MyProtocol
  (my-type [x] (str "type of input is a string")))

(my-type "oi")

(extend-protocol MyProtocol
  BigDecimal
  (my-type [x] (str "type of input is bigdec"))

  java.lang.Long
  (my-type [x] (str "type of input is long"))

  java.lang.Object
  (my-type [x] (str "x could be anything!")))

(my-type "oi")
(my-type 3M)
(my-type 3)
(my-type 0.1)                                               ; default implementation

;4. Create a role-playing game that implements behavior using multiple
;dispatch
(defrecord Player [name class])

(defmulti cast-spell (fn [player] (:class player)))
(defmethod cast-spell :mage
  [player] (str (:name player) " used fireball!"))
(defmethod cast-spell :warlock
  [player] (str (:name player) " used shadow bolt!"))
(defmethod cast-spell :priest
  [player] (str (:name player) " used flash heal!"))
(defmethod cast-spell :default
  [player] (str (:name player) " punched the foe!"))

(def player1 (->Player "John" :priest))
(def player2 (->Player "Mary" :warrior))
(def player3 (->Player "Joseph" :mage))
(def player4 (->Player "Tom" :warlock))

(cast-spell player1)                                        ; priest -> flash heal
(cast-spell player2)                                        ; warrior -> default action
(cast-spell player3)                                        ; mage -> fire ball
(cast-spell player4)                                        ; warlock -> shadow bolt
