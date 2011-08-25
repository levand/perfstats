(ns histograms-by-operation
  (:use [incanter core stats charts])
  (:require [clojure.pprint :as pp]))

(println (count user/*samples*))
(pp/pprint (first user/*samples*))


(def samples
     ($where {:latency {:$lt 1000}} (dataset (keys (first user/*samples*)) user/*samples*)))

(def operations ($group-by [:label] samples))

(doseq [[{label :label} ds] operations]
  (with-data ds
    (view (histogram ($ :latency) :nbins 50 :title label))))