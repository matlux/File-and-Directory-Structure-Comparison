(ns net.matlux.filecomparator.ssh-adapter
  (:use clj-ssh.ssh)
  (:use [clojure.string :only (join split)])
  (:use [net.matlux.filecomparator.core :only (diff-set-dir-analysis format-file-diff-set remove-prefix-coll)])
)

(comment
(with-ssh-agent []
  (add-identity "/Users/mathieu/.ssh/id_rsa_no_pass")
  (let [session (session "192.168.77.14" :strict-host-key-checking :no)]
    (with-connection session
      (let [result (ssh session "find /lauramathieu/photos/photos2011/children/Louis/May/.AppleDouble -exec md5sum {} \\;" :result-map true)]
        result))))
)

(defn format-search-cmd-line [^String path]
  (format "find %s -exec md5sum {} \\;" path))

(defn raw-ssh-md5-result [ssh-path]
  (with-ssh-agent []
    (add-identity (:identity ssh-path))
    (let [session (session (:hostname ssh-path) :strict-host-key-checking :no)]
      (with-connection session
        (let [result (ssh session (format-search-cmd-line (:path ssh-path)) :result-map true)]
          result)))))

(defn raw-ssh-md5-result-old [identity-file ^String hostname ^String path]
  (with-ssh-agent []
    (add-identity identity-file)
    (let [session (session hostname :strict-host-key-checking :no)]
      (with-connection session
        (let [result (ssh session (format-search-cmd-line path) :result-map true)]
          result)))))




(defn permutate-pairs-and-remove-prefix [coll prefix-length]
  (interleave (remove-prefix-coll (take-nth 2 (rest coll))  prefix-length) (take-nth 2 coll) ))
;(map #(remove-prefix path-length %) (map #(.toString %) file-list))


(defn convert-md5-result-into-hash-map [raw-string  prefix-length]
  (apply hash-map (permutate-pairs-and-remove-prefix (split raw-string #"\s+")  prefix-length)))


(defn map-file-digest-via-ssh [ssh-path]
  (convert-md5-result-into-hash-map (second (raw-ssh-md5-result ssh-path)) (.length (ssh-path :path))))


(comment

(def ssh-path1 {:identity "/Users/mathieu/.ssh/id_rsa_no_pass" :hostname "192.168.77.14" :path "/lauramathieu/photos/photos2011/children/Louis/May/.AppleDouble"})  
(def ssh-path2 {:identity "/Users/mathieu/.ssh/id_rsa_no_pass" :hostname "192.168.77.19" :path "/backup/readynas/lauramathieu/photos/photos2011/children/Louis/May/.AppleDouble"})

(map-file-digest-via-ssh  ssh-path1)

)


(defn diff-file-dir-via-ssh [ssh-path1 ssh-path2]
  (let [file-map1 (map-file-digest-via-ssh ssh-path1)
        file-map2 (map-file-digest-via-ssh ssh-path2)
        parsed-map (diff-set-dir-analysis file-map1 file-map2)]
    (format-file-diff-set parsed-map ))) 
  
  
(comment
  
  
  
(diff-file-dir-via-ssh  ssh-path1 ssh-path2)
)