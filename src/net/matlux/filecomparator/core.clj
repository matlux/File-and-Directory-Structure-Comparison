(ns net.matlux.filecomparator.core
  (:use [net.matlux.filecomparator.ssh-adapter :only (map-file-digest-via-ssh)])
  (:use [net.matlux.filecomparator.local-adapter :only (map-file-digest file?)])
  (:use [net.matlux.filecomparator.common :only (diff-set-dir-analysis format-file-diff-set)])
  )
;(in-ns 'net.matlux.filecomparator.app)


  

(defn diff-file-dir [path1 path2]
  (let [file-map1 (map-file-digest path1 file? "SHA1")
        file-map2 (map-file-digest path2 file? "SHA1")
        parsed-map (diff-set-dir-analysis file-map1 file-map2)]
    (format-file-diff-set parsed-map ))) 
  
(defmulti digest-target :proto)
(defmethod digest-target "local" [file-path-target] (map-file-digest (:path file-path-target) file? "SHA1"))
(defmethod digest-target "ssh" [ssh-path-target]    (map-file-digest-via-ssh ssh-path-target))


(defn diff-targets [target1 target2]
  (let [digested-files1 (digest-target target1)
        digested-files2 (digest-target target2)
        parsed-map (diff-set-dir-analysis digested-files1 digested-files2)]
    (format-file-diff-set parsed-map ))) 

  
(comment
(diff-file-dir "treestructure/tree1/" "treestructure/tree2/")
)
  
