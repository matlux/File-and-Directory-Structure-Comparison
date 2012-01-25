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

(defn
  #^{:test (fn []
             (assert (= ["ring1" "ring2" "ring3" "ring4"] (update-nth2 #(apply str (drop 2 %)) 1 ["string1" "string2" "string3" "string4"])))
             (assert (= ["string1" "ring2" "string3" "ring4"] (update-nth2 #(apply str (drop 2 %)) 2 ["string1" "string2" "string3" "string4"])))
             (assert (= ["string1" "string2" "ring3" "string4" "string5" "ring6"] (update-nth2 #(apply str (drop 2 %)) 3 ["string1" "string2" "string3" "string4" "string5" "string6"])))
             (assert (= [1 3 3 5 5 7] (update-nth2 inc 2 [1 2 3 4 5 6])))
             (assert (= [1 2 4 4 5 7] (update-nth2 inc 3 [1 2 3 4 5 6])))
             )}
    update-nth2 [f n coll] (mapcat #(update-in (vec %) [(dec n)] f) (partition n coll)))

(defn 
  #^{:test (fn []
             (assert (= ["ring1" "ring2" "ring3" "ring4"] (update-nth #(apply str (drop 2 %)) 1 ["string1" "string2" "string3" "string4"])))
             (assert (= ["ring1" "string2" "ring3" "string4"] (update-nth #(apply str (drop 2 %)) 2 ["string1" "string2" "string3" "string4"])))
             (assert (= ["ring1" "string2" "string3" "ring4" "string5" "string6"] (update-nth #(apply str (drop 2 %)) 3 ["string1" "string2" "string3" "string4" "string5" "string6"])))
             (assert (= [2 2 4 4 6 6] (update-nth inc 2 [1 2 3 4 5 6])))
             (assert (= [2 2 3 5 5 6] (update-nth inc 3 [1 2 3 4 5 6])))
             )}
  update-nth [f n coll]
     (let [f2 (fn [[a & restcoll]] (cons (f a) restcoll))
           ]
       (apply-nth f2 n coll)))


(defn convert-md5-result-into-hash-map [raw-string  to-drop]
  (let [s (->> (split raw-string #"\n+") (map #(split % #"\s+" 2)) (apply concat)) ;(split raw-string #"\s+")
        d #(->> (drop to-drop %) (apply str))]
    (->> (apply-nth reverse 2 s) (update-nth d 2) (apply hash-map))))


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


