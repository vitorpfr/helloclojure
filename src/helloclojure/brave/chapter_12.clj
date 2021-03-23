(ns helloclojure.brave.chapter-12
  (:import [java.util Date Stack]
           [java.net Proxy URI]))

; java usage/interop

(.toUpperCase "By Bluebeard's bananas!")

(.indexOf "Let's synergize our bleeding edges" "y")

(Math/abs -3)

; all of those are macroexpansions of the dot special form
(macroexpand-1 '(.toUpperCase "By Bluebeard's bananas!"))


; creating objects
(new String)
(String.)

; dot form is more used
(String. "To Davey Jones's Locker with ye hardies")
; equals to
(str "To Davey Jones's Locker with ye hardies")

(let [stack (java.util.Stack.)]
  (.push stack "Latest episode of Game of Thrones, ho!")
  stack)

; execute multiple methods to same object
(doto (java.util.Stack.)
  (.push "Latest episode of Game of Thrones, ho!")
  (.push "Whoops, I meant 'Land, ho!'"))


; imports
(import [java.util Date Stack]
        [java.net Proxy URI])

(Date.)

; commonly used java classes
; system class
(System/getenv)

(System/getProperty "user.dir")
(System/getProperty "java.version")

; date class
(def my-date #inst "2016-09-19T20:40:02.733-00:00")
(type my-date)                                              ; it is a java.util.Date

; files and input/output
(let [file (java.io.File. "/")]
  (println (.exists file))                                  ; true
  (println (.canWrite file))                                ; false
  (println (.getPath file)))                                ; "/"

; but i cant read or write to it!
; to read a file, i need to use java.io.BufferedReader or java.io.FileReader
; to write a file, i need to use java.io.BufferedWriter or java.io.FileWriter

; however, clojure already has built-in functions to read and write such as split and slurp
; spit writes to file
(spit "/tmp/hercules-todo-list"
      "- kill dat lion brov
      - chop up what nasty multi-headed snake thing")

; slurp reads file
(slurp "/tmp/hercules-todo-list")


(let [s (java.io.StringWriter.)]
  (spit s "- capture cerynian hind like for real")
  (.toString s))


(let [s (java.io.StringReader. "- get erymanthian pig what with the tusks")]
  (slurp s))


(with-open [todo-list-rdr (clojure.java.io/reader "/tmp/hercules-todo-list")]
  (println (first (line-seq todo-list-rdr))))
