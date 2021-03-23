(ns helloclojure.brave.chapter-9)

; future creates another thread, so the sleep (other thread) doesn't block the second print (main thread)
(future (Thread/sleep 4000)
        (println "I'll print after 4 seconds"))
(println "I'll print immediately")


; future returns a reference that you can use to request the result (cached)
(let [result (future (println "this prints once")
                     (+ 1 1))]
  (println "deref: " (deref result))
  (println "@: " @result))

; deref (@) blocks the current thread until the result is returned
(let [result (future (Thread/sleep 3000)
                     (+ 1 1))]
  (println "The result is: " @result)
  (println "It will be at least 3 seconds before I print"))

; if you don't want to wait for the deref, you can pass a default value (this returns 5 if the result is not returned after 10 ms
(deref (future (Thread/sleep 1000) 0) 10 5)

; you can use realized? to check if a future is done running
(realized? (future (Thread/sleep 1000)))                    ; false

(let [f (future)]
  @f
  (realized? f))                                            ; true

; delay: only executed when forced/deref
(def jackson-5-delay
  (delay (let [message "Just call my name and I'll be there"]
           (println "First deref:" message)
           message)))

; like a future, the result is cached: the first print is done only in the first deref
(force jackson-5-delay)

; this creates a delay to e-mail user, which is not executed immediately
; then runs a future (separate thread) for each document uploaded
; when it finishes doc upload, it notifies user
(def gimli-headshots ["serious.jpg" "fun.jpg" "playful.jpg"])
(defn email-user
  [email-address]
  (println "Sending headshot notification to" email-address))
(defn upload-document
  "Needs to be implemented"
  [headshot]
  true)
(let [notify (delay (email-user "and-my-axe@gmail.com"))]
  (doseq [headshot gimli-headshots]
    (future (upload-document headshot)
            (force notify))))
; however, user it notified only in the first upload, because result of delay is cached


; promises states that I expect a result here without mentioning first what it is
(def my-promise (promise))
(deliver my-promise (+ 1 2))
@my-promise                                                 ; returns 3

; if I try to deref the promise before delivering, it locks the thread until it is delivered
(def my-other-promise (promise))
;@my-other-promise ; this blocks the thread because the promise wasn't delivered

; promise use case: find the first satisfactory element in a collection of data
(def yak-butter-international
  {:store      "Yak Butter International"
   :price      90
   :smoothness 90})
(def butter-than-nothing
  {:store      "Butter Than Nothing"
   :price      150
   :smoothness 83})
;; This is the butter that meets our requirements
(def baby-got-yak
  {:store      "Baby Got Yak"
   :price      94
   :smoothness 99})
(defn mock-api-call
  [result]
  (Thread/sleep 1000)
  result)
(defn satisfactory?
  "If the butter meets our criteria, return the butter, else return false"
  [butter]
  (and (<= (:price butter) 100)
       (>= (:smoothness butter) 97)
       butter))

; this takes 3s because it runs in one thread, so takes 1s for each API call to check
(time (some (comp satisfactory? mock-api-call)
            [yak-butter-international butter-than-nothing baby-got-yak]))

; this takes 1s because it runs multiple threads, one API call/consult on each
(time
  (let [butter-promise (promise)]
    (doseq [butter [yak-butter-international butter-than-nothing baby-got-yak yak-butter-international]]
      (future (if-let [satisfactory-butter (satisfactory? (mock-api-call butter))]
                (deliver butter-promise satisfactory-butter))))
    (println "And the winner is:" @butter-promise)))

; I could have done this without a promise, but then it would return all satisfactory results (promise would get the first and cache)

; also, if none of the results is satisfactory the thread would be blocked - to avoid that, you can include a timeout:
(let [p (promise)]
  (deref p 100 "timed out"))

; I can also use promises to register callbacks
; the future starts immediately, but it is blocked by the promise
; after 100ms, the promise is delivered and the future is unblocked (so this line basically runs async)
(let [ferengi-wisdom-promise (promise)]
  (future (println "Here's some Ferengi wisdom:" @ferengi-wisdom-promise))
  (Thread/sleep 100)
  (deliver ferengi-wisdom-promise "Whisper your way to success."))


; use case: create a "queue" of tasks that need to be serialized (done in a specific order)
(defmacro wait
  "Sleep `timeout` seconds before evaluating body"
  [timeout & body]
  `(do (Thread/sleep ~timeout) ~@body))

(let [saying3 (promise)]
  (future (deliver saying3 (wait 100 "Cheerio!")))
  @(let [saying2 (promise)]
     (future (deliver saying2 (wait 400 "Pip pip!")))
     @(let [saying1 (promise)]
        (future (deliver saying1 (wait 200 "'Ello, gov'na!")))
        (println @saying1)
        saying1)
     (println @saying2)
     saying2)
  (println @saying3)
  saying3)

; this can be extracted to a macro
(defmacro enqueue
  ([q concurrent-promise-name concurrent serialized]
   `(let [~concurrent-promise-name (promise)]
      (future (deliver ~concurrent-promise-name ~concurrent))
      (deref ~q)
      ~serialized
      ~concurrent-promise-name))
  ([concurrent-promise-name concurrent serialized]
   `(enqueue (future) ~concurrent-promise-name ~concurrent ~serialized)))

; now we can serialize the tasks (guarantee that they happen in a specific order)!
; even though the actions have different sleeps, they are all handled concurrently and the print happened in order
(time @(-> (enqueue saying (wait 200 "'Ello, gov'na!") (println @saying))
           (enqueue saying (wait 400 "Pip pip!") (println @saying))
           (enqueue saying (wait 100 "Cheerio!") (println @saying))))



