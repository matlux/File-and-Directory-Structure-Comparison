(ns net.matlux.filecomparator.ssh-adapter
  (:use clj-ssh.ssh)
  (:use [clojure.string :only (join split)])
  (:use [clojure.set :only (difference intersection)])
  (:use [clojure.pprint])
  (:use [net.matlux.filecomparator.common :only (diff-set-dir-analysis format-file-diff-set)])
)
;(in-ns 'net.matlux.filecomparator.app)



(defn format-search-cmd-line [^String path]
  (format "find %s -type f -exec md5sum {} \\;" path))

(defn raw-ssh-md5-result [ssh-path]
  (with-ssh-agent []
    (add-identity (:identity ssh-path))
    (let [session (session (:hostname ssh-path) :strict-host-key-checking :no)]
      (with-connection session
        (let [result (ssh session (format-search-cmd-line (:path ssh-path)) :result-map true)]
          result)))))

;(defn permutate-pairs-and-remove-prefix [coll prefix-length]
;  (interleave (remove-prefix-coll (take-nth 2 (rest coll))  prefix-length) (take-nth 2 coll) ))

(defn apply-nth [f n coll]
  (->> (partition n coll) (mapcat f)))

(defn update-nth2 [f n coll] (mapcat #(update-in (vec %) [(dec n)] f) (partition n coll)))

(defn update-nth [f n coll]
     (let [f2 (fn [[a & restcoll]] (cons (f a) restcoll))
           ]
       (apply-nth f2 n coll)))

;(defn update-nth [f n coll]
;     (let [f2 (fn [[a & restcoll]] (cons (f a) restcoll))
;           ]
;       (apply-nth f2 n coll)))

(defn convert-md5-result-into-hash-map [raw-string  to-drop]
  (let [s (->> (split raw-string #"\n+") (map #(split % #"\s+" 2)) (apply concat)) ;(split raw-string #"\s+")
        d #(->> (drop to-drop %) (apply str))]
    (->> (apply-nth reverse 2 s) (update-nth d 2) (apply hash-map))))

;(defn convert-md5-result-into-hash-map [raw-string  prefix-length]
;  (let [s (split raw-string #"\s+")]
;  (apply hash-map (permutate-pairs-and-remove-prefix s prefix-length))))

(def bleep 7)
(defn raw-ssh-cmd [ssh-path cmd]
  (with-ssh-agent []
    (add-identity (:identity ssh-path))
    (let [session (session (:hostname ssh-path) :strict-host-key-checking :no)]
      (with-connection session
        (let [result (ssh session (format cmd (:path ssh-path)) :result-map true)]
          result)))))

(defn map-file-digest-via-ssh [ssh-path]
  (convert-md5-result-into-hash-map (second (raw-ssh-md5-result ssh-path)) (.length (ssh-path :path))))

(defn map-file-digest-via-ssh-mock [ssh-path]
  (convert-md5-result-into-hash-map (second (raw-ssh-cmd ssh-path "find %s -type f -exec echo 1234567 {} \\;")) (.length (ssh-path :path))))


(defn diff-file-dir-via-ssh [ssh-path1 ssh-path2]
  (let [file-map1 (map-file-digest-via-ssh ssh-path1)
        file-map2 (map-file-digest-via-ssh ssh-path2)
        parsed-map (diff-set-dir-analysis file-map1 file-map2)]
    (format-file-diff-set parsed-map ))) 
  
(defn raw-ssh-find-result [ssh-path]
  (with-ssh-agent []
    (add-identity (:identity ssh-path))
    (let [session (session (:hostname ssh-path) :strict-host-key-checking :no)]
      (with-connection session
        (let [result (ssh session (format "find %s" (:path ssh-path)) :result-map true)]
          result)))))


