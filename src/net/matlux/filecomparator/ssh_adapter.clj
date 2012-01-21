;(ns net.matlux.filecomparator.ssh-adapter
;  (:use clj-ssh.ssh)
;  (:use [clojure.string :only (join split)])
;  (:use [net.matlux.filecomparator.core :only (diff-set-dir-analysis format-file-diff-set remove-prefix-coll)])
;)
(in-ns 'net.matlux.filecomparator.app)

(declare diff-set-dir-analysis format-file-diff-set remove-prefix-coll)

(defn format-search-cmd-line [^String path]
  (format "find %s -exec md5sum {} \\;" path))

(defn raw-ssh-md5-result [ssh-path]
  (with-ssh-agent []
    (add-identity (:identity ssh-path))
    (let [session (session (:hostname ssh-path) :strict-host-key-checking :no)]
      (with-connection session
        (let [result (ssh session (format-search-cmd-line (:path ssh-path)) :result-map true)]
          result)))))

(defn permutate-pairs-and-remove-prefix [coll prefix-length]
  (interleave (remove-prefix-coll (take-nth 2 (rest coll))  prefix-length) (take-nth 2 coll) ))

(defn convert-md5-result-into-hash-map [raw-string  prefix-length]
  (apply hash-map (permutate-pairs-and-remove-prefix (split raw-string #"\s+")  prefix-length)))

(defn map-file-digest-via-ssh [ssh-path]
  (convert-md5-result-into-hash-map (second (raw-ssh-md5-result ssh-path)) (.length (ssh-path :path))))

(defn diff-file-dir-via-ssh [ssh-path1 ssh-path2]
  (let [file-map1 (map-file-digest-via-ssh ssh-path1)
        file-map2 (map-file-digest-via-ssh ssh-path2)
        parsed-map (diff-set-dir-analysis file-map1 file-map2)]
    (format-file-diff-set parsed-map ))) 
  
