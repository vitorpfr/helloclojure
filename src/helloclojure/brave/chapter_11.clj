(ns helloclojure.brave.chapter-11
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]))

; start of "playsync" project

; creates a channel named echo-chan
(def echo-chan (chan))

; creates a new process
; when I take a message from echo-chan, print it
(go (println (<! echo-chan)))
; <! is the take function - it is listening to the echo-chan channel

; >!! puts the string "ketchup" in the channel
(>!! echo-chan "ketchup")
; since there's a process listening to the channel, it takes the message immediately and prints "ketchup"

; this puts blocks the repl indefinitely, because there's no process listening to this new channel
(comment (>!! (chan) "mustard"))

; creating a channel with a buffer size of two
(def echo-buffer (chan 2))
(>!! echo-buffer "ketchup")
(>!! echo-buffer "ketchup")
(comment (>!! echo-buffer "ketchup"))                       ; this blocks because channel buffer is full

; >! parking put, <! parking take (both should be used inside (go))
; >!! blocking put, <!! blocking take
; parking moves between threads, blocking blocks the current one

; thread creates a new thread and executes a process in that thread
(thread (println (<!! echo-chan)))
(>!! echo-chan "mustard")


; hot-dog machine process
; it waits for money and returns a hot-dog
(defn hot-dog-machine
  []
  (let [in (chan)
        out (chan)]
    (go (<! in)
        (>! out "hot dog"))
    [in out]))

(let [[in out] (hot-dog-machine)]
  (>!! in "pocket lint")
  (<!! out))

; issue: current hot dog machine accepts anything as input, and also delivers only one hot dog

(defn hot-dog-machine-v2
  [hot-dog-count]
  (let [in (chan)
        out (chan)]
    (go (loop [hc hot-dog-count]
          (if (> hc 0)
            (let [input (<! in)]
              (if (= 3 input)
                (do (>! out "hot dog")
                    (recur (dec hc)))
                (do (>! out "wilted lettuce")
                    (recur hc))))
            (do (close! in)
                (close! out)))))
    [in out]))

(let [[in out] (hot-dog-machine-v2 2)]
  (>!! in "pocket lint")
  (println (<!! out))
  (>!! in 3)
  (println (<!! out))
  (>!! in 3)
  (println (<!! out))
  (>!! in 3)
  (<!! out))

; you can have process outputs as other processes input (a process pipeline)
(let [c1 (chan)
      c2 (chan)
      c3 (chan)]
  (go (>! c2 (clojure.string/upper-case (<! c1))))
  (go (>! c3 (clojure.string/reverse (<! c2))))
  (go (println (<! c3)))
  (>!! c1 "redrum"))                                        ; prints MURDER


; The core.async function alts!! lets you use the result of the first successful
;channel operation among a collection of operations
(defn upload
  [headshot c]
  (go (Thread/sleep (rand 100))
      (>! c headshot)))

; getting the one that completed first
(let [c1 (chan)
      c2 (chan)
      c3 (chan)]
  (upload "serious.jpg" c1)
  (upload "fun.jpg" c2)
  (upload "sassy.jpg" c3)
  (let [[headshot channel] (alts!! [c1 c2 c3])]
    (println "Sending headshot notification for" headshot)))

; same, but with a timeout
(let [c1 (chan)]
  (upload "serious.jpg" c1)
  (let [[headshot channel] (alts!! [c1 (timeout 20)])]
    (if headshot
      (println "Sending headshot notification for" headshot)
      (println "Timed out!"))))
