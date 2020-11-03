(ns helloclojure.brave.chapter-6)

; namespace is a object of class clojure.lang.Namespace
; ns contains map between human-friendly symbols and references
; to addresses (vars)

; get current namespace
(ns-name *ns*)

; returns address of fn in the core namespace (var)
inc

; returns inc symbol
'inc



; stores an object in this namespace (interning a var)
; and gives it a symbol (great-books)
(def great-books ["East of Eden" "The Glass Bead Game"])

; check content of object/var
great-books

; check map of symbols-to-interned-vars in the current ns
(ns-interns *ns*)

; get a specific var
(get (ns-interns *ns*) 'great-books)

; get whole map of symbols to vars
(ns-map *ns*)

; get a specific var from symbols-to-vars map
(get (ns-interns *ns*) 'great-books)

; deref var to get the object they point to
(deref #'helloclojure.brave.chapter-6/great-books)
; this is the same of just using the symbol
great-books
; which means: retrieve the var associated with the symbol great-books
; and deref it

; now we'll call def again with the same symbol
(def great-books ["The Power of Bees" "Journey to Upstairs"])
great-books
; the var has been updated with the address of the new vector
; name collision: we can no longer find the first vector in memory!
; (it became garbage in memory)


; fully qualified symbol:
clojure.core/inc
cheese.taxonomy/bries

; create and move to namespace
(in-ns 'cheese.analysis)

; refer to symbols in other namespace
(clojure.core/refer 'cheese.taxonomy)
; it updates the current namespace symbol/object map, adding an entry:
 {bries #'cheese.taxonomy/bries}
; even though i;m in the cheese.analysis ns

; private function: available only on its namespace
;defn- or defn ^:private

; alias: shorten namespace to be used in fully qualified symbols
(clojure.core/alias 'taxonomy 'cheese.taxonomy)
; then i can use taxonomy/bries to call bries

; continue on 'to Catch a Burglar'' which uses the divine cheese code folder