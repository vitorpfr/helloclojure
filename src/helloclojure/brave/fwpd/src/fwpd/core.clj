(ns fwpd.core
  (:gen-class))

(def filename "suspects.csv")

(def vamp-keys [:name :glitter-index])

(defn str->int
  [str]
  (Integer. str))

(def conversions {:name identity
                  :glitter-index str->int})

(defn convert
  [vamp-key value]
  ((get conversions vamp-key) value))


(defn parse
  "convert a CSV into rows of columns"
  [string]
  (map #(clojure.string/split % #",")
       (clojure.string/split string #"\n")))


(defn mapify
  "Return a seq of maps like {:name \"Edward Cullen\" :glitter-index 10}"
  [rows]
  (map (fn [unmapped-row]
         (reduce (fn [row-map [vamp-key value]]
                   (assoc row-map vamp-key (convert vamp-key value)))
                 {}
                 (map vector vamp-keys unmapped-row)))
       rows))


(defn glitter-filter
  [minimum-glitter records]
  (filter #(>= (:glitter-index %) minimum-glitter) records))


;; Exercises
; 1) Turn the result of your glitter filter into a list of names.

(defn glitter-filter
  [minimum-glitter records]
  (let [filter-result (filter #(>= (:glitter-index %) minimum-glitter) records)]
    (map :name filter-result)))

; 2) Write a function, append, which will append a new suspect to your list of suspects.
; Obs: My implementation redefined `subject` - another way to do is to just conj the new suspect

(defn append [suspects new-suspect]
  "appends new suspect in the format {:name \"Test\" :glitter-index 5}"
  (def suspects (conj suspects new-suspect)))

; 3) Write a function, validate, which will check that :name and :glitter-index are present when you append. The validate function should accept two arguments: a map of keywords to validating functions, similar to conversions, and the record to be validated.

(def validation-function {:name (fn [record] (contains? record :name))
                          :glitter-index (fn [record] (contains? record :glitter-index))})

;almost right - just need to do both keys at the same time
(defn validate [keyword->val-fn record]
  ((get keyword->val-fn :name) record)
  )

; trying to fix
(defn validate [keyword->val-fn record]
  (map (fn [row]
         (get keyword->val-fn (keys row)))
       record)
  )


;Write a function that will take your list of maps and convert it back to a CSV string. You’ll need to use the clojure.string/join function.

