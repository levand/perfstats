(ns scatterplot-by-thread
  (:use [incanter core stats charts])
  (:require [clojure.pprint :as pp]))

(println (nil? user/*samples*))

(pp/pprint (first user/*samples*))

(print (count user/*samples*))


(def samples (dataset (keys (first user/*samples*)) user/*samples*))

(def operations ($group-by [:label] samples))

(defn is-valid-set
  "Determines if this set has enough data to care about"
  [ds]
  (< 5 (count (:rows ds))))

(doseq [[{op-label :label} op-ds] operations]
  (if (< 5 (count (:rows op-ds)))
    (let [chart (scatter-plot :start-time :latency :title op-label :legend true :data (dataset []))]
      (doseq [[{g :group-threads} d] ($group-by [:group-threads] op-ds)]
        (if (< 5 (count (:rows d)))
          (add-points chart :start-time :latency :data d :series-label (str g " threads"))))
      (view chart))))