(defproject perfstats "1.0.0-SNAPSHOT"
  :description "A JMeter plugin providing arbitrary statistical analysis of performance test results using Clojure and Incanter"
  :dev-dependencies [[robert/hooke "1.1.0"]]
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [incanter "1.2.3"]]
  :hooks [perfstats.leinhook]
  :aot [perfstats.PerfstatVisualizer])
