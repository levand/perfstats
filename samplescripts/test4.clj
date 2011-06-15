(ns test4
  (:use [incanter core stats charts])
  (:require [clojure.pprint :as pp]))

(pp/pprint (first user/*samples*))

(def my-samples (map (fn [r] (assoc r :threadcount (Integer/parseInt (re-find #"^\d+" (:thread-name r))))) user/*samples*))
(def samples (dataset (keys (first my-samples)) (drop 3 my-samples)))


(println (distinct (map :group-threads my-samples)))


(defn report-group [key group]
  (with-data group
    (println "key: " key)
    (println "Count: " (count ($ :latency)))))

(def trimmed-data ($where {:latency {:$lt (first  (quantile ($ :latency samples) :probs [0.99]))}} samples))

(with-data trimmed-data

  (view (scatter-plot ($ :latency) ($ :threadcount) ))

  (let [groups ($group-by [:threadcount])]
    (doseq [[k v] groups]
      (report-group k v)))
  
  
  (println "Groups" (count ($group-by [:group-threads])))
  (println "Number of samples:" (count ($ :latency)))
  (println "Quantile of latency:" (quantile ($ :latency) :probs [0.99 0.95 0.9]))
  (println "Min Latency:" (reduce min ($ :latency)))
  (println "Max latency:" (reduce max ($ :latency)))
  (println "Mean latency:" (mean ($ :latency)))
  (println "Standard deviation:" (sd ($ :latency))))

