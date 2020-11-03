(ns helloclojure.brave.chapter-8)

;; writing macros
; macros are very important, ex:
; is a macro wrote in terms of 'if' and 'do'!

(macroexpand '(when boolean-expression expression-1
                                       expression-2
                                       expression-3))

; macros are very similar to functions
; however, they return a list (this is what clojure evals)

; macros arguments are not evaluated (they are in functions)
(defmacro infix
  "Use this macro when you pine for the notation of your childhood" [infixed]
  (list (second infixed) (first infixed) (last infixed)))

(macroexpand '(infix (1 + 1)))

; macros can also use destructuring and recursion
; 'or' 'and' are macros that use recursion
(macroexpand '(and 1 true false))


; creating a macro that both prints a value and returns it
(comment
  (defmacro println-returning
    [expression]
    (list let [result expression]
          (list println result)
          result))

  (println-returning "oi"))
; this returns a error
;Can't take the value of a macro: #'clojure.core/let
; because the macro tries to get the value that the symbol let refers to
; in this case I want to return the let symbol itself
; ' (quote) turns off evaluation and just returns the symbol itself
; so it will be evaluated in the next step (eval)

(defmacro println-returning
  [expression]
  (list 'let ['result expression]
        (list 'println 'result)
        'result))

(println-returning "oi")

; quote returns a unevaluated symbol
+                                                           ;plus function
(quote +)                                                   ;plus symbol

; any-symbol                                                  ;random symbol value (not associated to a value)
(quote any-symbol)                                          ;random symbol (just the symbol itself)

; this (') is normal quoting

; now we'll go through syntax quote
; it returns a fully qualified symbol (with namespace included)

; returns +
'+

; returns clojure.core/+
`+

; quoting a list recursively quotes all the elements
'(+ 1 2)

; syntax quoting a list recursively syntax quotes all the elements
`(+ 1 2)
; syntax code is better to avoid namespace collisions
; syntax code also has tilde (~) to unquote specific forms

`(+ 1 ~(inc 1))
; this returns (+ 1 2), because ~ makes (inc 1) to be evaluated

; example showing same struct, to show syntax quote is easier to read
(list '+ 1 (inc 1))
`(+ 1 ~(inc 1))

(defn criticize-code
  [criticism code]
  `(println ~criticism (quote ~code)))

(criticize-code "a" '(+ 1 1))

; unquote splicing: unwraps a seqable data structure and places its
;  content within the enclosing syntax-quoted data structure
; returns list of + and a list inside
`(+ ~(list 1 2 3))

; returns list of + and the arguments exploded
`(+ ~@(list 1 2 3))

; using everything together to improve a macro
; from this
(defmacro code-critic
  [bad good]
  `(do ~(criticize-code "Cursed bacteria of Liberia, this is bad code:" bad)
       ~(criticize-code "Sweet sacred boa of Western and Eastern Samoa, this is good code:" good)))

; to this
(defmacro code-critic
  [{:keys [good bad]}]
  `(do ~@(map #(apply criticize-code %)
              [["Sweet lion of Zion, this is bad code:" bad]
               ["Great cow of Moscow, this is good code:" good]])))

; ~@ is necessary to avoid ending up with (do '(nil nil))
; having that, we would try to call nil as a function (first arg of list), and this gives exception
; thats why we need to unwrap the map result!

(code-critic {:good (+ 1 1)
              :bad  (1 + 1)})

; playing with macros

; prints and returns result
(defmacro my-print-return
  [text]
  (list 'let ['result text]
        (list 'println 'result)
        'result))

(my-print-return "hi")

; improved version
; needed to add the # at the end of 'result' to work
; reason: https://clojure.org/guides/weird_characters#gensym
; using syntax quote (`) transforms all symbols inside in fully resolved symbols
; but the symbol inside let must not be fully resolved (let clojure generate a unqualified symbol)
(defmacro my-print-return-improved
  [text]
  `(let [result# ~text]
     (println result#)
     result#))

(my-print-return-improved "hi")

(defmacro my-print
  "receives a words and print them sequentially"
  [word]
  `(println ~word))

; equals to
(defmacro my-print
  "receives a words and print them sequentially"
  [word]
  (list 'println word))

(my-print "oi, mo")

(defmacro my-print-list
  "receives a list of words and print them sequentially"
  [list-of-words]
  `(println ~@list-of-words))

(my-print-list ["oi" "mo"])

; Things to watch out for

; variable capture: when the macro uses a binding the user doesn't know

(def message "Good job!")
(defmacro with-mischief
  [& stuff-to-do]
  (concat (list 'let ['message "Oh, big deal!"])
          stuff-to-do))



; macro usage uses the internal let instead of
(with-mischief
  (println "Here's how I feel about that thing you did: " message))

; if i try to use syntax quote, let doesn't work
; reason: syntax quote returns fully resolved symbol, and let should be a new unique symbol
; this means using syntax quote avoids variable capture, because it forces us to assign a new unique symbol for local bindings
; solution: either use (gensym) to generate new unique symbol or autogensym (binding#)

; double evaluation: happens when a macro argument is evaluated more than once

; checks if arg is truthy
(defmacro report [to-try]
  `(if ~to-try
     (println (quote ~to-try) "was successful:" ~to-try)
     (println (quote ~to-try) "was not successful:" ~to-try)))

; tihs would sleep for 2s because the macro evaluates it twice
; once on if, then again on println
(report (do (Thread/sleep 1000) (+ 1 1)))

; how to avoid: use let with a new unique symbol
(defmacro report
  [to-try]
  `(let [result# ~to-try]
     (if result#
       (println (quote ~to-try) "was successful:" result#)
       (println (quote ~to-try) "was not successful:" result#))))
; now we'll evaluate the code once and bind it to a autogensym'd symbol


; macros all the way down
; issue: using doseq to run a macro multiple times
; it fails because macro receives unevaluated symbols (and doseq binds them on eval)

; solution is to create a specific macro to expand desired behavior on seqs

;;;; USE CASE TO WRITE MACROS

;; Validation function

; this order map has an invalid e-mail
(def order-details
  {:name  "Mitchard Blimmons"
   :email "mitchard.blimmonsgmail.com"})

; ideally, we want to write code that produces something like this
(comment
  (validate order-details order-details-validation))
; => {:email ["Your email address doesn't look like an email address."]}

; map of validations
(def order-details-validations
  {:name  ["Please enter a name" not-empty]
   :email ["Please enter an email address" not-empty

           "Your e-mail address doesn't look like an email address"
           #(or (empty? %) (re-seq #"@" %))]})

; fn that applies validation to a single value
(defn error-messages-for
  "Return a seq of error messages"
  [to-validate message-validator-pairs]
  (map first (filter #(not ((second %) to-validate))
                     (partition 2 message-validator-pairs))))

; complete validation function
(defn validate
  "Returns a map with a vector of errors for each key"
  [to-validate validations]
  (reduce (fn [errors validation]
            (let [[fieldname validation-check-groups] validation
                  value (get to-validate fieldname)
                  error-messages (error-messages-for value validation-check-groups)]
              (if (empty? error-messages)
                errors
                (assoc errors fieldname error-messages))))
          {}
          validations))

order-details
order-details-validations

(validate order-details order-details-validations)

; now we can use the function to validate fields, in the following pattern:
(let [errors (validate order-details order-details-validations)]
  (if (empty? errors)
    (println :success)
    (println :failure errors)))

; but this is not good and would be repeated a lot in the code
; idea: build a if-valid function that abstracts the repetitive part
(defn if-valid
  [record validations success-code failure-code]
  (let [errors (validate record validations)]
    (if (empty? errors) success-code failure-code)))
; but this wouldn't work because success-code and failure-code would be evaluated each time
; and the idea is not to evaluate it (just pass forward)

; macros are the solution for this! since we can modify code before evaluation
(defmacro if-valid
  "Handles validation more concisely"
  [to-validate validations errors-name & then-else]
  `(let [~errors-name (validate ~to-validate ~validations)]
     (if (empty? ~errors-name)
       ~@then-else)))

; maacroexpand that shows my-error-name is a symbol to have the error result binded to
(macroexpand
  '(if-valid order-details order-details-validations my-error-name
             (println :success)
             (println :failure my-error-name)))

; using the macro to validate order-details
; errors is a symbol that will have the result binded to inside the macro
(if-valid order-details order-details-validations errors
          (println :success)
          (println :failure errors))

; exercises
; 1
; first writing my 'when' macro
(defmacro my-when
  [condition & actions]
  `(if ~condition
     (do ~@actions)))

; then writing when-valid based on if-valid
; writing two options of the macro: one using regular quote and one using syntax quote

; when-valid with syntax quote - first option
(defmacro when-valid
  "Handles validation more concisely"
  [to-validate validations & actions]
  `(if (empty? (validate ~to-validate ~validations))
     (do ~@actions)))

; when-valid with list and regular quote - second option
(defmacro when-valid
  "Handles validation more concisely"
  [to-validate validations & actions]
  (list 'if (list 'empty? (list 'validate to-validate validations))
        (cons 'do actions)))

; testing when-valid with a non-valid case (returns nil)
(when-valid order-details order-details-validations
            (println "It's a success!")
            (println :success))

; testing when-valid with a valid case (does the body)
(def valid-order-details
  {:name  "Mitchard Blimmons"
   :email "mitchard.blimmons@gmail.com"})

(when-valid valid-order-details order-details-validations
            (println "It's a success!")
            (println :success))

; 2
; first let's understand or behavior
; or returns first truthy value or the last value of the collection
(or 1 2 false)                                              ; 1
(or 1 2)                                                    ; 1
(or false nil false 4 nil)                                  ; 4
(or nil false)                                              ; false
(or false nil)                                              ; nil

; here's and for reference
(defmacro my-and
  ([] nil)
  ([x] x)
  ([x & next]
   `(let [y# ~x]
      (if y# (and ~@next) y#))))
; how it works:
; binds first argument (x) to y#
; if y# is truthy returns and applied to rest
; if y# is falsy returns y#

; in the case of or, we want to return the first arg if it is truthy
(defmacro my-or
  ([] nil)
  ([x] x)
  ([head & tail]
   `(let [y# ~head]
      (if y# y# (or ~@tail)))))

; testing
(my-or 1 2 false)                                           ; 1
(my-or 1 2)                                                 ; 1
(my-or false nil false 4 nil)                               ; 4
(my-or nil false)                                           ; false
(my-or false nil)                                           ; nil


; 3
(def character {:name       "Smooches McCutes"
                :attributes {:intelligence 10
                             :strength     4
                             :dexterity    5}})

((comp :intelligence :attributes) character)

(defn attr [attribute]
  (comp attribute :attributes))

; in practice
(comment
  (def c-int (attr :intelligence))
  (c-int character))

; objective: build a macro 'defattrs' that defines c-int, c-str, c-dex all at once


; building a complex macro, one step at a time
; simple macro that links a symbol to a value
(defmacro test-macro
  [name body]
  `(def ~name ~body))
(test-macro one 1)

; same macro above, but receives args as a list
(defmacro test-macro-2
  [args]
  `(def ~@args))
(test-macro-2 [one 1])

(defn create-var
  [[name val]]
  (list 'def name val))

; same macro above, but applies a map to the list of args
(defmacro test-macro-3
  [args]
  `(do
     ~@(map #(create-var %)
            args)))
(test-macro-3 [[one 5]])
(test-macro-3 [[one 6]
               [two 4]
               [three 2]])

; applying what worked in the problem context - result of exercise
; function that returns a list with def to be evaluated
(defn def-attribute-fn
  [[name attr]]
  (list 'def name (comp attr :attributes)))

; maps function above to all combinations
(defmacro defattrs
  [& args]
  `(do
     ~@(map #(def-attribute-fn %)
            (partition 2 args))))

; testing result of exercise
(def character {:name       "Smooches McCutes"
                :attributes {:intelligence 10
                             :strength     4
                             :dexterity    5}})

(defattrs c-int :intelligence
          c-str :strength
          c-dex :dexterity)

(c-int character)
(c-str character)
(c-dex character)