(println "LOADING HOOKE")
(require 'robert.hooke)
(require 'leiningen.classpath)

(def jmeter-home (System/getenv "JMETER_HOME"))

(defn find-jars-in-dir [directory]
  (let [dir (java.io.File. directory)]
    (filter (fn [filename] (.endsWith filename ".jar"))
            (map (fn [f] (.getCanonicalPath f)) (seq (.listFiles dir))))))

(robert.hooke/add-hook
 #'leiningen.classpath/get-classpath
 (fn [get-classpath project]
   (println "HOOKING")
   (reduce conj (get-classpath project) (concat (find-jars-in-dir (str jmeter-home "/lib/ext"))
                                                (find-jars-in-dir (str jmeter-home "/lib"))))))