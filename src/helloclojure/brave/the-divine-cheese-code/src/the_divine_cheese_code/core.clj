(ns the-divine-cheese-code.core
  (:gen-class))

;; Ensure that the SVG code is evaluated
(require 'the-divine-cheese-code.visualization.svg)

;; Refer the namespace so that you don't have to use the
;; fully qualified name to reference svg functions
(refer 'the-divine-cheese-code.visualization.svg)

(def heists [{:location    "Cologne, Germany"
              :cheese-name "Archbishop Hildebold's Cheese Pretzel"
              :lat         50.95
              :lng         6.97}
             {:location    "Zurich, Switzerland"
              :cheese-name "The Standard Emmental"
              :lat         47.37
              :lng         8.55}
             {:location    "Marseille, France"
              :cheese-name "Le Fromage de Cosquer"
              :lat         43.30
              :lng         5.37}
             {:location    "Zurich, Switzerland"
              :cheese-name "The Lesser Emmental"
              :lat         47.37
              :lng         8.55}
             {:location    "Vatican City"
              :cheese-name "The Cheese of Turin"
              :lat         41.90
              :lng         12.45}])

(defn -main
  [& args]
  (println (points heists)))

; we could have aliased the namespace while requiring as well
; tihs
(require '[the-divine-cheese-code.visualization.svg :as svg])
; is equal to this
(require 'the-divine-cheese-code.visualization.svg)
(alias 'svg 'the-divine-cheese-code.visualization.svg)

; 'use' is the same as using 'require' and 'refer' at the same time
; (read and evaluate the file corresponding to namespace, and then refer all symbols inside it
; 'use' shouldn't be used in prod! Only on development


; ns refers clojure.core by default, that's why we don't need to refer it

; (:refer-clojure :exclude [min max])
; this, inside ns, doesn'' get the functions min and max from clojure.core