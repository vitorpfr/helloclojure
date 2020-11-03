(ns helloclojure.brave.chapter-7)

; macros allow you to transform arbitrary expressions into valid clojure

(defmacro backwards
  [form]
  (reverse form))

(backwards (" backwards" " am" " I" str))


; going deeper on why this works

; clojure transforms a function into a list, which is passed to
; the clojure evaluator
;(+ 1 2) -> [+ 1 2] -> goes to clojure compiler -> returns 3

; we can use 'eval' to evaluate directly data structures
(def addition-list (list + 1 2))
(eval addition-list)

; or simply
(eval '(+ 1 2))

; doing more complex stuff - adding a number to the sum
(eval (concat addition-list [10]))

; defining a symbol using eval
(eval (list 'def 'lucky-number (concat addition-list [10])))
lucky-number

; repl means read-eval-print-loop
; read and eval are independent processes (as demonstrated above)
; 1) read
; the reader: converts textual file into clojure data structures

; this textual representation is the "reader form"
(str "To understand what recursion is," " you must first understand recursion.")

; read-string is a function who receives unicode text (that we write) and returns the resulting data structure
; returns a list '(+ 1 2)
(read-string "(+ 1 2)")

; returns true since it is a list
(list? (read-string "(+ 1 2)"))

; returns a list with :zagglewag as the first element (the function)
; '(:zagglewag + 1 2)
(conj (read-string "(+ 1 2)") :zagglewag)

; if i want to also eval the result list - this returns 3
(eval (read-string "(+ 1 2)"))

; those examples are 1 to 1 between reader and data structure
; some are not, ex: anonymous function
(read-string "#(+ 1 %)")
; this string is read into a list containing fn*, a vector with a symbol
; and a list containing three elements

; anonymouns fn uses a reader macro to transform text into data structures
; macro characters: ', @ and #

; quote reader-macro expands the single-quote character
(read-string "'(a b c)")

;'sth expands to (quote sth)
;@sth expands to (deref sth)
;#(sth %) expands to (fn [arg] (sth arg))

; 2) evaluate
; the evaluator - function that takes a data structure and returns a result
; ex: receives (list + 1 2) or '(+ 1 2), returns 3

; any data structure that is not a list or symbol evalutes to itself
true
{}
:foo

;; clojure evals symbols by: special form, local binding, namespace mapping, throws exception (in this order)
; special form: used in the context of operation, first element
(if true :a :b)

; local binding: association between a symbol and a value that wasn't created by def
(let [x 5]
  (+ x 3))

; + symbol is not the same as the function that it refers to
(read-string "+")

; symbol type is clojure.lang.Symbol
(type (read-string "+"))

; here i'm dealing with the plus symbol as a data structure
; and not with the addition function that it refers to

; if I evaluate it, clojure looks up the function and applies it
(eval (list (read-string "+") 1 2))

; if, quote, loop, def are all special forms: they are not evaluated as normal functions

; ex: quote just returns the data structure itself instead of evaluating it

;; macros
; macros can manipulate data structures that clojure evaluates
;ex: infix addition - creating a macro to use (1 + 2) instead of (+ 1 2)

; creating a list that represents infix addition
(read-string "(1 + 1)")

; clojure will throw an exception if i try to evaluate
;(eval (read-string "(1 + 1)"))

; since read-string returns a list, I can use clojure to reorganize args
(let [infix (read-string "(1 + 1)")]
  (list (second infix) (first infix) (last infix)))

; if I eval this, it returns the result
(eval
  (let [infix (read-string "(1 + 1)")]
    (list (second infix) (first infix) (last infix))))

; macros are executed beetween reader and evaluator
; they receive unevaluated data structure and pass it forward


(defmacro ignore-last-operand [function-call]
  (butlast function-call))

; this receives (+ 1 2 10) as arg, and not 13!
; this is why macros operate between read and eval
(ignore-last-operand (+ 1 2 10))

;; This will not print anything
(ignore-last-operand (+ 1 2 (println "look at me!!!")))

; macroexpand: determines the return value of a macro before eval
(macroexpand '(ignore-last-operand (+ 1 2 10)))
; this returns (+ 1 2)

; this is the macro to do infix operations
(defmacro infix
  [infixed]
  (list (second infixed)
        (first infixed)
        (last infixed)))

(infix (1 + 2))

; Exercises
; those focus on read and eval
; 1
(eval (list println "Vitor" "Star Wars"))
(eval '(println "Vitor" "Star Wars"))
(eval (read-string "(println \"Vitor\" \"Star Wars\")"))

; 2
(read-string "(1 + 3 * 4 - 5)")

(defmacro custom-infix
  [[number1 plus number2 times number3 minus number4]]
  (list minus (list plus number1 (list times number2 number3)) number4))

(custom-infix (1 + 3 * 4 - 5))
(macroexpand '(custom-infix (1 + 3 * 4 - 5)))





