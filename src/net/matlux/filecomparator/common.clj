(ns net.matlux.filecomparator.common
  (:use [clojure.set :only (difference intersection)])
  (:use [clojure.pprint])
  )


(defn diff-set-dir-analysis [tree1 tree2]
  (let [k1 (set (keys tree1))
        v1 (set (vals tree1))
        k2 (set (keys tree2))
        v2 (set (vals tree2))
        only-k1 (difference k1 k2)
        only-k2 (difference k2 k1)
        present-but-different (filter #(not= (tree1 %) (tree2 %)) (intersection k1 k2))]
    {:only-k2 only-k2 :only-k1 only-k1 :present-but-different (map #(list % (tree1 %) (tree2 %)) present-but-different)}))



(defn format-file-diff-set [parsed-map]
  (let [only-k1 (:only-k1 parsed-map)
        only-k2 (:only-k2 parsed-map)
        present-but-different (:present-but-different parsed-map)]
    
    (println "\n****** FILE DIFFERENCE REPORT: ******")
    (when (and (= only-k1 #{}) (= only-k2 #{}) (= present-but-different ()))
      (println "No difference found between the file(s)"))
    (cl-format nil "~{file ~a is missing in set #1~%~}~{file ~a is missing in set #2~%~}~{~{file ~a is different~%  -> ~a~%  -> ~a~%~}~}"
               only-k2
               only-k1
               present-but-different)))



  