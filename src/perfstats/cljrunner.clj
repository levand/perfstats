(ns perfstats.cljrunner
  (:import (java.awt BorderLayout Color Component Dimension Image)
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

(defn execute-button
  "Creates the button that, when pressed, executes the script"
  [fileselector output-callback tabpane]
  (JButton. "Execute"))

(defn output-panel
  "Creates a scrollable output panel, returns a tuple of the panel and a function that can be called to set its contents."
  []
  (let [textarea (JTextArea.)]
    (.setEditable textarea false)
    [(JScrollPane. textarea)
     (fn [text] (.append textarea text))]))

(defn panel
  "Returns a Panel with the UI for Clojure execution"
  []
  (let [panel (JPanel. (BorderLayout.))
        fileselector (FilePanel. "Select a Clojure script" "clj")
        tabs (JTabbedPane.)
        output (output-panel)
        output-panel (first output)]

    (.addTab tabs "Output" output-panel)
        
    (.add fileselector (execute-button fileselector (fn [] "Hello, world!") tabs))
    (.add panel fileselector BorderLayout/NORTH)
    (.add panel tabs BorderLayout/CENTER)
    panel))