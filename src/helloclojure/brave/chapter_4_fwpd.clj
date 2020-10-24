(ns helloclojure.brave.chapter-4-fwpd)

; source code
(def filename "src/helloclojure/brave/suspects.csv")

; this returns the content unparsed
(slurp filename)

(def vamp-keys [:name :glitter-index])

(defn str->int
  [str]
  (Integer. str))

(def conversions {:name          identity
                  :glitter-index str->int})

(defn convert
  [vamp-key value]
  ((get conversions vamp-key) value))

(defn parse
  "Convert a CSV into rows of columns"
  [string]
  (map #(clojure.string/split % #",")
       (clojure.string/split string #"\n")))

; testing that data is parsed
(parse (slurp filename))

(defn mapify
  "Return a seq of maps like {:name \"Edward Cullen\" :glitter-index 10}"
  [rows]
  (map (fn [unmapped-row]
         (reduce (fn [row-map [vamp-key value]]
                   (assoc row-map vamp-key (convert vamp-key value)))
                 {}
                 (map vector vamp-keys unmapped-row)))
       rows))

; testing fn
(first (mapify (parse (slurp filename))))

; takes vampire records (map) and filters out those with a :glitter-index
; less than minimum-glitter
(defn glitter-filter
  [minimum-glitter records]
  (filter #(>= (:glitter-index %) minimum-glitter) records))

; this returns only the vampires with glitter-index >= 3
(glitter-filter 3 (mapify (parse (slurp filename))))


; exercises - will build upon the code above

; 1
;(->> (glitter-filter 3 (mapify (parse (slurp filename))))
;     (map :name))
; another form
(->> (slurp filename)
     parse
     mapify
     (glitter-filter 3)
     (map :name))

; 2
(defn append
  "Append a new suspect to the record of suspects"
  [records suspect]
  (conj records suspect))

(def new-suspect {:name          "John"
                  :glitter-index 8})

(-> (slurp filename)
    parse
    mapify
    (append new-suspect))

; 3
(def conversions {:name          identity
                  :glitter-index str->int})


(def other-new-suspect {:name          "John"
                        :glitter-index "8"})

(defn validate
  [record conversion-map]
  (and (= (keys conversion-map) (keys record))
       (reduce (fn [new-record [key val]]
                 (when-let [conversion-fn (get conversion-map key)]
                   (assoc new-record key (conversion-fn val))))
               {}
               record)))

; testing
(validate other-new-suspect conversions)
(validate {:name "Vitor"} conversions)

; redefining append, now with validation
(defn append-with-validation
  "Try to append a new suspect to the record of suspects"
  [records suspect]
  (if-let [validated-record (validate suspect conversions)]
    (conj records validated-record)
    records))

; this should work
(-> (slurp filename)
    parse
    mapify
    (append-with-validation other-new-suspect))

; this shouldn't do anything (returns the records unchanged) - suspect is incomplete
(-> (slurp filename)
    parse
    mapify
    (append-with-validation {:name "Vitor"}))

; 4
(defn records-to-csv
  [records]
  (->> records
       (map vals)
       (map #(clojure.string/join "," %))
       (clojure.string/join "\n")))

; testing
(-> (slurp filename)
    parse
    mapify
    (append-with-validation other-new-suspect)
    records-to-csv
    (#(spit "src/helloclojure/brave/new-suspects.csv" %)))