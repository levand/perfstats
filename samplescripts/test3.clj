(ns test3
  (:use [incanter core stats charts])
  (:require [clojure.pprint :as pp]))

(def samples (dataset (keys (first user/*samples*)) (drop 1 user/*samples*)))

(pp/pprint (first user/*samples*))

(with-data samples
  (view (scatter-plot ($ :start-time) ($ :latency)))
  (view (histogram ($ :latency) :nbins 50)))



