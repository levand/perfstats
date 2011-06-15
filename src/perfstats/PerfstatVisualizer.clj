(ns perfstats.PerfstatVisualizer
  (:require [perfstats.cljrunner :as cljrunner]
            [clojure.string :as s])
  (:import (java.awt BorderLayout Color Component Dimension Image)
           (javax.swing BorderFactory Box JComponent JLabel JPanel JTextField)
           (javax.swing.border BevelBorder Border EmptyBorder)
           (org.apache.jorphan.logging LoggingManager)
           (org.apache.log Logger)
           (org.apache.jmeter.samplers Clearable SampleResult)
           (org.apache.jmeter.protocol.http.sampler HTTPSampleResult)
           (org.apache.jmeter.util JMeterUtils)
           (org.apache.jmeter.testelement TestElement)
           (org.apache.jmeter.gui.util FilePanel)
           (org.apache.jmeter.visualizers.gui AbstractVisualizer))
  (:gen-class
   :extends org.apache.jmeter.visualizers.gui.AbstractVisualizer
   :implements [org.apache.jmeter.samplers.Clearable]
   :state state
   :init init-state
   :post-init init
   :exposes-methods {makeTitlePanel makeTitlePanelSuper
                     add addSuper}))

(def logger (LoggingManager/getLoggerFor "perfstats.PerfstatVisualizer"))
(def label "Perfstats")

(defn parse-http-headers
  "Parses HTTP headers"
  [header-str]
  (into {}
        (filter #(= 2 (count %))
                (map (fn [h] (s/split h #": " 2))
                     (s/split header-str #"\n")))))

(defprotocol Sample
  (to-map [sample] "Loads a sample into a map"))

(extend-type SampleResult
  Sample
  (to-map [sample] {:start-time (.getStartTime sample)
                    :end-time (.getEndTime sample)
                    :label (.getSampleLabel sample)
                    :successful (.isSuccessful sample)
                    :code (.getResponseCode sample)
                    :url (.getUrlAsString sample)
                    :monitor (.isMonitor sample)
                    :group-threads (.getGroupThreads sample)
                    :thread-name (.getThreadName sample)
                    :latency (.getLatency sample)
                    :headers (parse-http-headers (.getResponseHeaders sample))}))

(defn add-sample
  "Given a full state map, adds a sample to it"
  [state sample]
  (assoc-in state [:samples] (conj (:samples state) sample)))

;;; Overridden methods

(defn -init-state []
  "Initializes our class's state"
  [[] (ref {:samples []})])

;; defined in :methods
(defn -add
  "Method called when a sample is added"
  [this ^SampleResult result]
  (.info logger (str "sample:" (class result)))
  (dosync
   (commute (.state this) add-sample (to-map result))))

;; defined in getstat
(defn -getStaticLabel
  "Gets the visualizer element's static label"
  [this]
  label)

(defn -getLabelResource
  "Gets the visualizer element's label"
  [this]
  label)

(defn -clearData [this]
  (println "current state:" @(.state this))
  (println "clear"))

(defn -init [this]
  (.info logger "initialized perfstat!")
  (.setLayout this (BorderLayout.))
  (.setBorder this (EmptyBorder. 10 10 5 10))
  (.addSuper this (.makeTitlePanelSuper this) BorderLayout/NORTH)
  (.addSuper this (cljrunner/panel (.state this))))