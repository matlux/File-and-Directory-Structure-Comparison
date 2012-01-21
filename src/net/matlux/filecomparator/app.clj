(ns net.matlux.filecomparator.app
  (:gen-class)
  ;(:use net.matlux.filecomparator.core)
  (:use clojure.tools.cli)
  (:use clj-ssh.ssh)
  (:use [clojure.string :only (split)]))

(declare diff-targets)

(load "core")
(load "ssh_adapter")

(defn parse-url2 [url]
  (let [reg [#"(\w+)://(.+)@(\w+):(.+)"
             #"(\w+)@(.+):(.+)"
             #"(.+):(.+)"
             #"(.+)"]
        result (->> (map #(re-seq % url) reg) (filter identity) first first rest reverse 
          (interleave [:path :hostname :user :proto])
                   )
        ]
    result))

(defn parse-url [url]
  (let [f (fn ([t u h p] [t u h p])
              ([u h p] ["ssh" u h p])
              ([h p] ["ssh" nil h p])
              ([p] ["local" nil nil p]))
        
        res [#"(\w+)://(.+)@(\w+):(.+)"
             #"(\w+)@(.+):(.+)"
             #"(.+):(.+)"
             #"(.+)"]]
    (->> (map #(re-seq % url) res) (filter identity) first first rest (apply f) (interleave [:proto :user :hostname :path]) (apply hash-map))))

          
(comment
(parse-url "ssh://user@hostname:/path/number2")
(parse-url "user@hostname:/path/number2")
(parse-url "192.168.1.1:/path/number2")
(parse-url "/path/number2")
["ssh" "user" "hostname" "/path/number2"]
["ssh" "user" "hostname" "/path/number2"]
["ssh" nil "192.168.1.1" "/path/number2"]
["local" nil nil "/path/number2"]
)



(defn parse-cmd-line [& args]
  (let [[options args banner] 
        (cli args
             ["-p" "--port" "use this port" :parse-fn #(Integer. %) :default 22] 
             ;["-h" "--host" "The hostname" :default "localhost"]
             ;["-v" "--[no-]verbose" :default true]
             ["-i" "--identity" :default "~/.ssh/id_rsa"]
             )
        [source destination] (->> (map parse-url args) (map #(merge options %)))]
        (when (:help options)
          (println banner)
          (System/exit 0))
        (println (format "options=%s\nargs=%s" options args))
        (println (format "source=%s\ndestination=%s" source destination))
        
        (println (apply diff-targets [source destination]))))
        
(comment
(parse-cmd-line "--ssh-identity" "/Users/mathieu/.ssh/id_rsa_no_pass" "treestructure/tree1/" "hostname:treestructure/tree2/")
  
)


(defn -main [& args]
  (println args)
  (println (apply parse-cmd-line args)))


