(ns perf-by-thread
  (:use [incanter core stats charts])
  (:require [clojure.pprint :as pp]))

(pp/pprint (first user/*samples*))

(defn normalize-samples [samples]
     (let [origin (reduce min (map :start-time samples))]
       (map #(update-in % [:start-time] - origin) samples)))

(def my-samples (normalize-samples user/*samples*))

(def samples (dataset (keys (first my-samples)) my-samples))

(def operations ($group-by [:label] samples))


(defn is-valid-set
  "Determines if this set has enough data to care about"
  [[{groups-label :group-threads} ds]]
  (< 5 (count (:rows ds))))


(defn calc-throughput1
  "Calculates throughput based on the sum of latencies"
  [threads ds]
  (let [aggregate-latency (reduce + ($ :latency ds))
        estimated-time (/ aggregate-latency threads)
        ms-per-record (/ estimated-time (count (:rows ds)))
        throughput-hr (/ (* 1000.0 60 60) ms-per-record)]
    throughput-hr))


(defn build-meta-ds
  [all-data]
  (map (fn [[{threads :group-threads} ds]]
         (with-data ds
           (let [quantiles (quantile ($ :latency) :probs [0.999 0.99 0.95 0.9])] 
               {:threads threads
                :throughput (calc-throughput1 threads $data)
                :sample-count (count (:rows $data))
                :min-latency (reduce min ($ :latency))
                :max-latency (reduce max ($ :latency))
                :mean-latency (mean ($ :latency))
                :median-latency (median ($ :latency))
                :std-dev-latency (sd ($ :latency))
                :percentile-999 (nth quantiles 0)
                :percentile-99 (nth quantiles 1)
                :percentile-95 (nth quantiles 2)
                :percentile-90 (nth quantiles 3)
                :error-rate (/ (count (:rows ($where {:successful {:$eq false}})))
                               (count (:rows $data)))})))       
       (filter is-valid-set ($group-by [:group-threads] all-data))))


(defn operation-latency-chart [op-ds label]
  (let [datamap (build-meta-ds op-ds)
        ds ($order :threads :asc (dataset (keys (first datamap)) datamap))]
    (with-data ds
      (view
       (doto (line-chart :threads :mean-latency :series-label "mean latency" :legend true :title label)
         (add-categories :threads :median-latency :series-label "median latency")
         (add-categories :threads :percentile-90 :series-label "90th percentile")
         (add-categories :threads :percentile-95 :series-label "95th percentile")
         (add-categories :threads :percentile-99 :series-label "99th percentile")
         (add-categories :threads :percentile-999 :series-label "99.9th percentile")
         (add-categories :threads :error-rate :series-label "Error rate")
         (add-categories :threads :throughput :series-label "Throughput/hr")
         )))))

(defn print-summary [op-ds op-label]
  (println "------" op-label "------")
  (with-data op-ds
    (let [latency ($ :latency)
          quantiles (quantile latency :probs [0.999 0.99 0.95 0.9])]
      (println "Load:" (reduce min ($ :group-threads)) "to" (reduce max ($ :group-threads)) "threads.")
      (println "mean latency: " (mean latency))
      (println "median latency: " (median latency))
      (println "latency std deviation: " (sd latency))
      (println "max latency: " (reduce max latency))
      (println "min latency: " (reduce min latency))
      (println "99.9 percentile latency" (nth quantiles 0))
      (println "99 percentile latency" (nth quantiles 1))
      (println "95 percentile latency" (nth quantiles 2))
      (println "90 percentile latency" (nth quantiles 3))
      (println "error rate" (double (/ (count (:rows ($where {:successful {:$eq false}})))
                                      (count (:rows $data))))))))

(doseq [[{op-label :label} op-ds] operations]
  (print-summary op-ds op-label)
  (operation-latency-chart op-ds op-label))