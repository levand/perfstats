(ns test5
  (:use [incanter core stats charts])
  (:require [clojure.pprint :as pp]))

(pp/pprint (first user/*samples*))

(def samples (dataset (keys (first user/*samples*)) user/*samples*))

(with-data samples
  (view (scatter-plot ($ :start-time) ($ :latency) :group-by ($ :group-threads)))
  )


(defn report-group [key group]
  (println "Group [" key "] had [" (count (:rows group)) "] samples"))

(with-data samples

  (let [groups ($group-by [:group-threads])]
    (doseq [[k v] groups]
      (report-group k v)))
  
  (println "Groups" (count ($group-by [:group-threads])))
  (println "Number of samples:" (count ($ :latency)))
  (println "Quantile of latency:" (quantile ($ :latency) :probs [0.99 0.95 0.9]))
  (println "Min Latency:" (reduce min ($ :latency)))
  (println "Max latency:" (reduce max ($ :latency)))
  (println "Mean latency:" (mean ($ :latency)))
  (println "Standard deviation:" (sd ($ :latency))))



(comment

(with-data group
    )


(doseq [v ($ :latency samples)]
      (println "class:" (class v)))









  )