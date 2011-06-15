(ns test2
  (:use [incanter core stats charts])
  (:require [clojure.pprint :as pp]))

(def samples (dataset (keys (first user/*samples*)) (drop 3 user/*samples*)))

(with-data samples
  (println "Number of samples:" (count ($ :latency)))
  (println "Quantile of latency:" (quantile ($ :latency) :probs [0.99 0.95 0.9]))
  (println "Min Latency:" (reduce min ($ :latency)))
  (println "Max latency:" (reduce max ($ :latency)))
  (println "Mean latency:" (mean ($ :latency)))
  (println "Standard deviation:" (sd ($ :latency))))

