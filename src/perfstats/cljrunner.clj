(ns perfstats.cljrunner
  (:import (java.io Writer StringWriter PrintWriter)
           (java.awt BorderLayout Color Component Dimension Image)
           (java.awt.event ActionListener ActionEvent)
           (javax.swing BorderFactory JTabbedPane JScrollPane Box JComponent JLabel JPanel JTextField JTextArea JButton)
           (javax.swing.border BevelBorder Border EmptyBorder)
           (org.apache.jorphan.logging LoggingManager)
           (org.apache.log Logger)
           (org.apache.jmeter.samplers Clearable SampleResult)
           (org.apache.jmeter.protocol.http.sampler HTTPSampleResult)
           (org.apache.jmeter.util JMeterUtils)
           (org.apache.jmeter.testelement TestElement)
           (org.apache.jmeter.gui.util FilePanel)
           (org.apache.jmeter.visualizers.gui AbstractVisualizer)))

(defmacro with-ns
  "Same as clojure.contrib.with-ns, copied as it is simpler than importing the lib."
  [ns & body]
  `(binding [*ns* (the-ns ~ns)]
     ~@(map (fn [form] `(eval '~form)) body)))

(with-ns 'user
  (def *filename* nil)
  (def *visualizer-state* nil)
  (def *samples* []))

(defn run-clojure-file
  "Executes a Clojure script. Returns the script's output. Must be passed a ref containing the visualizer's current state"
  [filename visualizer-state]
  (let [out (StringWriter.)
        err (StringWriter.)]
    (binding [*out* out
              *err* err
              user/*visualizer-state* visualizer-state
              user/*samples* (:samples @visualizer-state)
              user/*filename* filename]
      (try
        (with-ns 'user
          (load-file *filename*))
        (catch Throwable e
          (.printStackTrace e (PrintWriter. err)))
        ))
    (let [out-str (.toString out)
          err-str (.toString err)]
      (str out-str (if (.isEmpty err-str)
                     ""
                     (str "================================= ERRORS =====================================\n\n"
                          err-str))))))


(defn execute-button
  "Creates the button that, when pressed, executes the script.
Takes a ref representing the visualizer's state,
the function that should be called with the script's output,
as well as a handle to the tab, so the script can add visualizations to the results."
  [visualizer-state fileselector result-callback tabpane]
  (let [btn (JButton. "Execute")]
    (.setToolTipText btn "Execute the Clojure script. It will be executed in the classpath context of JMeter
 so put any libs you'd like to use in the JMETER_HOME/lib
The var user/*samples* will be bound to a seq of map object representing JMeter samples.")
    (.setActionCommand btn "clicked")
    (.addActionListener btn (proxy [ActionListener] []
                              (actionPerformed [evt]
                                               (if (= (.getActionCommand evt)  "clicked")
                                                 (result-callback (run-clojure-file (.getFilename fileselector) visualizer-state))))))

    btn))


(defn output-panel
  "Creates a scrollable output panel, returns a tuple of the panel and a function that can be called to set its contents."
  []
  (let [textarea (JTextArea.)]
    (.setEditable textarea false)
    [(JScrollPane. textarea)
     (fn [text]
       (.setText textarea "")
       (.append textarea text))]))

(defn panel
  "Returns a Panel with the UI for Clojure execution"
  [visualizer-state]
  (let [panel (JPanel. (BorderLayout.))
        fileselector (FilePanel. "Select a Clojure script" "clj")
        tabs (JTabbedPane.)
        output (output-panel)
        output-panel (first output)
        output-cb (second output)]
    
    (.addTab tabs "Output" output-panel)

    (.add fileselector (execute-button visualizer-state fileselector output-cb tabs))
    (.add panel fileselector BorderLayout/NORTH)
    (.add panel tabs BorderLayout/CENTER)
    panel))