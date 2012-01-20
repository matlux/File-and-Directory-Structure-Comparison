(ns net.matlux.filecomparator.core
  )

(use '[clojure.java.io :only (reader)])
(import '(java.io File))
(use '[clojure.pprint])
;(use 'clojure.contrib.pprint)
(use '[clojure.set])

(defn xml-file? [file] (.contains (.toString file) ".xml"))
(defn jar-file? [file] (.contains (.toString file) ".jar"))
(defn file? [file] (.isFile file))

(defn list-of-xml-files [path] (filter xml-file? (file-seq (File. path))))

(defn list-of-filtered-files [path afilter] (filter afilter (file-seq (File. path))))

(defn count-file-lines [file] 
     (with-open [rdr (reader file)]
       (count (line-seq rdr))))

   
(defn md5sum
  "Returns the md5 hash of the contents of file (where file is a File,
   a FileDescriptor or a String with the file's path"
  [file]
  (with-open [input (java.io.FileInputStream. file)] 
    (let [digest (java.security.MessageDigest/getInstance "MD5")
        stream (java.security.DigestInputStream. input digest)
        bufsize (* 1024 1024)
        buf (byte-array bufsize)]
      (println (format "hashing file %s \t%d bytes" file (.length file)))
      (while (not= -1 (.read stream buf 0 bufsize)))
      (apply str (map (partial format "%02x") (.digest digest))))))
   
(defn sha1sum
  "Returns the md5 hash of the contents of file (where file is a File,
   a FileDescriptor or a String with the file's path"
  [file]
  (let [input (java.io.FileInputStream. file)
        digest (java.security.MessageDigest/getInstance "SHA1")
        stream (java.security.DigestInputStream. input digest)
        bufsize (* 1024 1024)
        buf (byte-array bufsize)]
      (while (not= -1 (.read stream buf 0 bufsize)))
      (apply str (map (partial format "%02x") (.digest digest)))))
 
(defn digestsum
  "Returns the md5 hash of the contents of file (where file is a File,
   a FileDescriptor or a String with the file's path"
  [file digest]
  (let [input (java.io.FileInputStream. file)
        digest (java.security.MessageDigest/getInstance digest)
        stream (java.security.DigestInputStream. input digest)
        bufsize (* 1024 1024)
        buf (byte-array bufsize)]
    (while (not= -1 (.read stream buf 0 bufsize)))
    (apply str (map (partial format "%02x") (.digest digest)))))


(defn map-file-md5 [file]
  (let [file-list (map #(.toString %) (list-of-xml-files "."))
        md5-list (map md5sum (list-of-xml-files "."))]
    (apply hash-map (interleave file-list md5-list))))


(defn remove-prefix [length val]
  (let [new-string (.substring val length)]
    new-string))

(defn remove-prefix-coll [string-coll length-to-remove]
  (map #(remove-prefix length-to-remove %) string-coll))

(defn map-file-digest [file-path file-filter digest]
     (let [file-list (list-of-filtered-files file-path file-filter)
           path-length (.length file-path)
           file-list-str (map #(remove-prefix path-length %) (map #(.toString %) file-list))
           md5-list (map md5sum file-list)]
       (apply hash-map (interleave file-list-str md5-list))))

(comment
(map-file-digest "." file? "SHA1")
(map-file-digest "." #(.isFile %)  "SHA1")
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



  

(defn diff-file-dir [path1 path2]
  (let [file-map1 (map-file-digest path1 file? "SHA1")
        file-map2 (map-file-digest path2 file? "SHA1")
        parsed-map (diff-set-dir-analysis file-map1 file-map2)]
    (format-file-diff-set parsed-map ))) 
  
  
(comment
(diff-file-dir "treestructure/tree1/" "treestructure/tree2/")
)
  
