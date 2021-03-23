(ns helloclojure.brave.chapter-10)

(def fred (atom {:cuddle-hunger-level  0
                 :percent-deteriorated 0}))

@fred

; updating one value to have fred's new state (F2)
(swap! fred
       (fn [current-state]
         (merge-with + current-state {:cuddle-hunger-level 1})))

@fred

; updating both at the same time to have fred's new state (F3)
(swap! fred
       (fn [current-state]
         (merge-with + current-state {:cuddle-hunger-level  1
                                      :percent-deteriorated 1})))


(defn increase-cuddle-hunger-level
  [zombie-state increase-by]
  (merge-with + zombie-state {:cuddle-hunger-level increase-by}))

; this line does not change fred state! We're just creating a new map with the current fred state
(increase-cuddle-hunger-level @fred 10)
@fred

; now we will generate a new fred state using swap
(swap! fred increase-cuddle-hunger-level 10)
@fred

; i could simplify the last call by using update
(swap! fred update :cuddle-hunger-level + 10)
@fred

; i can retain past state of the atom
(let [num (atom 1)
      s1 @num]
  (swap! num inc)
  (println "State 1:" s1)
  (println "Current state:" @num))

; reset! can be used to reset an atom value
(reset! fred {:cuddle-hunger-level  0
              :percent-deteriorated 0})

; I can use watches to watch when reference types state change (atom included)
; a watch fn always receives 4 args: a key for reporting, reference being watched, old state and new state
(defn shuffle-speed
  [zombie]
  (* (:cuddle-hunger-level zombie)
     (- 100 (:percent-deteriorated zombie))))

(defn shuffle-alert
  [key watched old-state new-state]
  (let [sph (shuffle-speed new-state)]
    (if (> sph 5000)
      (do
        (println "Run, you fool!")
        (println "The zombie's SPH is now " sph)
        (println "This message brought to your courtesy of " key))
      (do
        (println "All's well with " key)
        (println "Cuddle hunger: " (:cuddle-hunger-level new-state))
        (println "Percent deteriorated: " (:percent-deteriorated new-state))
        (println "SPH: " sph)))))

(reset! fred {:cuddle-hunger-level  22
              :percent-deteriorated 2})
(add-watch fred :fred-shuffle-alert shuffle-alert)

; after adding the watch, now every swap will report what happened!
(swap! fred update-in [:percent-deteriorated] + 1)
(swap! fred update-in [:cuddle-hunger-level] + 30)


; I can use a validator to specify what states are allowable for a reference
; swap! will only happen if the result of the validator on the new state is true
(defn percent-deteriorated-validator
  [{:keys [percent-deteriorated]}]
  (and (>= percent-deteriorated 0)
       (<= percent-deteriorated 100)))

(def bobby
  (atom
    {:cuddle-hunger-level 0 :percent-deteriorated 0}
    :validator percent-deteriorated-validator))

; this throws invalid reference state because percentage can't be above 100
(swap! bobby update-in [:percent-deteriorated] + 200)



; atoms are great to deal with independent entities
; however, sometimes I want events to update the state of more than one identity simultaneously
; for this, I can use refs

; example: modeling sock transfer (could be money transfer)
; refs are ACI:
; atomic (either all refs are updated or none are),
; consistent (refs will always have valid states - a sock will belong to a dryer or a gnome, but never both),
; isolated (events will happen always in sequence and one will not interfere in other)

; Clojure uses software transactional memory (STM) to implement this behavior
(def sock-varieties
  #{"darned" "argyle" "wool" "horsehair" "mulleted"
    "passive-aggressive" "striped" "polka-dotted"
    "athletic" "business" "power" "invisible" "gollumed"})

(defn sock-count
  [sock-variety count]
  {:variety sock-variety
   :count   count})

(defn generate-sock-gnome
  "Create an initial sock gnome state with no socks"
  [name]
  {:name  name
   :socks #{}})

; we'll start the gnome with no socks, and tghe drier with two socks
(def sock-gnome (ref (generate-sock-gnome "Barumpharumph")))
(def dryer (ref {:name  "LG 1337"
                 :socks (set (map #(sock-count % 2) sock-varieties))}))
@sock-gnome
@dryer

; now we want to make a transaction: dryer loses a sock and gnome gains the same sock
; we use alter to modify refs
; we must modify refs within a transaction (dosync)
(defn steal-sock
  [gnome dryer]
  (dosync
    (when-let [original-pair (some #(if (= (:count %) 2) %) (:socks @dryer))]
      (let [pair-with-one-sock-missing (update original-pair :count dec)]
        (alter gnome update-in [:socks] conj pair-with-one-sock-missing)
        (alter dryer update-in [:socks] disj original-pair)
        (alter dryer update-in [:socks] conj pair-with-one-sock-missing)))))

(steal-sock sock-gnome dryer)

; let's check which socks they have in common now
(defn similar-socks
  [target-sock sock-set]
  (filter #(= (:variety %) (:variety target-sock)) sock-set))

(similar-socks (first (:socks @sock-gnome)) (:socks @dryer))

; this shows that while the transaction is happening in another thread, the main therad doesn't have access to it
; it still prints 0 although the transaction is changing the value to 1 and 2
(def counter (ref 0))
(future
  (dosync
    (alter counter inc)
    (println @counter)
    (Thread/sleep 500)
    (alter counter inc)
    (println @counter)))
(Thread/sleep 250)
(println @counter)

; result: with refs, the transaction is only committed if it was successful on its compare and sets
; if other transaction changed the ref state meanwhile, the transaction itself will retry from the beginnnig

; we can use commute instead of alter, but it won't ever force a tx retry (it will commit the result directly)
; therefore it can be dangerous!

; safe use of commute:
(defn sleep-print-update
  [sleep-time thread-name update-fn]
  (fn [state]
    (Thread/sleep sleep-time)
    (println (str thread-name ": " state))
    (update-fn state)))
(def counter (ref 0))
(do
  (future (dosync (commute counter (sleep-print-update 100 "Thread A" inc))))
  (future (dosync (commute counter (sleep-print-update 150 "Thread B" inc)))))
; commute will always run the update function twice!

@counter

; unsafe use of commute
(def receiver-a (ref #{}))
(def receiver-b (ref #{}))
(def giver (ref #{1}))
(do (future (dosync (let [gift (first @giver)]
                      (Thread/sleep 10)
                      (commute receiver-a conj gift)
                      (commute giver disj gift))))
    (future (dosync (let [gift (first @giver)]
                      (Thread/sleep 50)
                      (commute receiver-b conj gift)
                      (commute giver disj gift)))))
; both receiver a and b end up with the number, which shouldn't happen! (invalid state)
@receiver-a
@receiver-b
@giver

;What’s different about this example is that the functions that are applied, essentially #(conj % gift) and #(disj % gift),
;are derived from the state of giver. Once giver
;changes, the derived functions produce an invalid state, but commute doesn’t
;care that the resulting state is invalid and commits the result anyway. The
;lesson here is that although commute can help speed up your programs, you
;have to be judicious about when to use it


; another example of commute
;; deciding whether to increment the counter takes the terribly long time
;; of 100 ms -- it is decided by committee.
(defn commute-inc! [counter]
  (dosync (Thread/sleep 100) (commute counter inc)))
(defn alter-inc! [counter]
  (dosync (Thread/sleep 100) (alter counter inc)))
(def counter (ref 0))

;; what if n people try to hit the counter at once? (pcalls uses future behing the scenes)
(defn bombard-counter! [n f counter]
  (apply pcalls (repeat n #(f counter))))

;; first, use alter.  Everyone is trying to update the counter, and
;; stepping on each other's toes, so almost every transaction is getting
;; retried lots of times:
(dosync (ref-set counter 0))
(time (doall (bombard-counter! 20 alter-inc! counter)))
;; note that it took about 2000 ms = (20 workers * 100 ms / update)

;; now, since it doesn't matter what order people update a counter in, we
;; use commute:
(dosync (ref-set counter 0))
(time (doall (bombard-counter! 20 commute-inc! counter)))
;; notice that we got actual concurrency this time (200 ms)

; summary: commute doesn't guarantee transaction order, but still guarantees transaction isolation


; var

; dynamic var
(def ^:dynamic *notification-address* "dobby@elf.org")

; you can temporarily change the value of dynamic vars (like using let)
(binding [*notification-address* "test@elf.org"]
  *notification-address*)


; use case: mocking value
(defn notify
  [message]
  (str "TO: " *notification-address* "\n"
       "MESSAGE: " message))

(notify "I fell.")

(binding [*notification-address* "test@elf.org"]
  (notify "test!"))

;Of course, you could have just defined notify to take an email address
;as an argument. In fact, that’s often the right choice. Why would you want
;to use dynamic vars instead?

;this is useful for clojure embedded dynamic vars, such as *out* for stdout
;we can use binding so print statements write to a file instead
(binding [*out* (clojure.java.io/writer "print-output")]
  (println "A man who carries a cat by the tail learns something he can learn in no other way.-- Mark Twain"))
(slurp "print-output")

(binding [*print-length* 1]
  (println ["Print" "just" "one!"]))

; this prints code to repl
(.write *out* "prints to repl")

; this doesn't print to repl because *out* is not bound to the repl printer in the new thread
(.start (Thread. #(.write *out* "prints to standard out")))

; weird workaround:
(let [out *out*]
  (.start
    (Thread. #(binding [*out* out]
                (.write *out* "prints to repl from thread")))))
; another possibility (bound-fn carries all current bindings to the new thread)
(.start (Thread. (bound-fn [] (.write *out* "prints to repl from thread"))))

; summary: bindings don’t get passed on to manually created threads. They do, however, get passed on to futures.


; when i use def, the initial value is the root
(def power-source "hair")
power-source

; i can use alter-var-root to change the root (not recommended usually)
(alter-var-root #'power-source (fn [_] "7-eleven parking lot"))
power-source


; temporary altering var root: with-redefs
(with-redefs [*out* *out*]
  (doto (Thread. #(println "with redefs allows me to show up in the REPL"))
    .start
    .join))
; difference from binding is that with-redefs will actually carry binding to child threads!
; therefore, it's better to use with-redefs than binding to set test environments!


; stateless concurrency and parallelism: pmap!
; like map, but clojure runs each application of the mapping function on a separate thread

; validating that pmap is faster than map for big collections
(defn always-1 [] 1)
(def alphabet-length 26)
;; Vector of chars, A-Z
(def letters (mapv (comp str char (partial + 65)) (range alphabet-length)))

(defn random-string
  "Returns a random string of specified length"
  [length]
  (apply str (take length (repeatedly #(rand-nth letters)))))

(defn random-string-list
  [list-length string-length]
  (doall (take list-length (repeatedly (partial random-string string-length)))))

(def orc-names (random-string-list 3000 7000))

(time (dorun (map clojure.string/lower-case orc-names)))    ; 119 ms
(time (dorun (pmap clojure.string/lower-case orc-names)))   ; 23 ms -> faster!

; however, pmap may be slower than map because of the overhead of thread creation/coordination
(def orc-name-abbrevs (random-string-list 40000 100))
(time (dorun (map clojure.string/lower-case orc-name-abbrevs))) ; 26 ms -> faster!
(time (dorun (pmap clojure.string/lower-case orc-name-abbrevs))) ; 70 ms

; it is possible to increase the grain size -> amount of work done by each parallelized task
; therefore reducing number of threads and reducing thread coordination overhead
; this can em done using partition-all, mapping and concatting the result
(time
  (dorun
    (apply concat
           (pmap (fn [name] (doall (map clojure.string/lower-case name)))
                 (partition-all 1000 orc-name-abbrevs)))))  ; 23 ms -> faster than normal map and pmap

; generalizing to a fn
(defn ppmap
  "Partitioned pmap, for grouping map ops together to make parallel
  overhead worthwhile"
  [grain-size f & colls]
  (apply concat
         (apply pmap
                (fn [& pgroups] (doall (apply map f pgroups)))
                (map (partial partition-all grain-size) colls))))

(time (dorun (ppmap 1000 clojure.string/lower-case orc-name-abbrevs))) ; 16 ms -> much faster

; reducers: faster operations than default reduce (https://clojure.org/reference/reducers)

; Exercises
; 1: Create an atom with the initial value 0, use swap! to increment it a
;couple of times, and then dereference it.
(do
  (def my-atom (atom 0))
  (swap! my-atom inc)
  (swap! my-atom inc)
  (swap! my-atom inc)
  @my-atom)

; 2:
(slurp "http://www.braveclojure.com/random-quote")
; not possible to do anymore because link is broken

; 3:
(defn new-character [name current-hp max-hp inventory]
  (ref {:name       name
        :current-hp current-hp
        :max-hp     max-hp
        :inventory  inventory}))

(def john (new-character "John" 30 30 [:healing-potion]))
(def mary (new-character "Mary" 15 40 []))

(defn in?
  [coll elm]
  (some #(= elm %) coll))

(defn has-potion?
  [source]
  (in? (:inventory @source) :healing-potion))

(defn remove-potion-from-inventory
  [inventory]
  (remove #(= :healing-potion %) inventory))

(defn heal-target
  [healing-amount {:keys [current-hp max-hp] :as target}]
  (assoc target :current-hp (min (+ healing-amount current-hp) max-hp)))

(defn use-potion
  [source target]
  (let [healing-amount 40]
    (if (has-potion? source)
      (dosync
        (alter source update :inventory remove-potion-from-inventory)
        (alter target (partial heal-target healing-amount)))
      (println "No potions available to be used"))))

(use-potion john mary)
@john
@mary
