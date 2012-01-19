(ns net.matlux.filecomparator.app
  (:gen-class)
  (:use net.matlux.filecomparator.core))

(defn -main [& args]
  (println args)
  (println (apply diff-file-dir args)))

