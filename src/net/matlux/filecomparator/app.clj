(ns net.matlux.filecomparator.app
  (:gen-class)
  (:use [net.matlux.filecomparator.core :only (diff-targets)])
  (:use clojure.tools.cli)
  (:use clj-ssh.ssh)
  (:use [clojure.string :only (split)]))




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
             ["-p" "--port" "not implemented yet" :parse-fn #(Integer. %) :default 22] 
             ;["-h" "--host" "The hostname" :default "localhost"]
             ["-h" "--help" "Show help" :default false :flag true]
             ["-i" "--identity" :default "~/.ssh/id_rsa"]
             )
        [source destination] (->> (map parse-url args) (map #(merge options %)))]
        (when (or (:help options) (not= (count args) 2))
          (println "NAME")
          (println "   fileComp.sh -- A file and directory structure comparison tool written in Clojure. Can work locally or remotely over ssh.\n")
          (println "SYNOPSIS")
          (println "   fileComp.sh [options] path1 path2\n")
          (println "EXAMPLES\n")
          (println "   local file comparison:")
          (println "      fileComp.sh treestructure/tree1 treestructure/tree2")
          (println "   ssh file comparison:")
          (println "      fileComp.sh --identity /home/username/.ssh/id_rsa treestructure/tree1 ssh://user@hostname:/tmp/treestructure/tree2\n")
          (println banner)
          (System/exit 0))
        (println (format "options=%s\nargs=%s" options args))
        (println (format "source=%s\ndestination=%s" source destination))
        
        (println (time (apply diff-targets [source destination])))
        (System/exit 0)))
        
(comment
(parse-cmd-line "--ssh-identity" "/Users/mathieu/.ssh/id_rsa_no_pass" "treestructure/tree1/" "hostname:treestructure/tree2/")
  
)


(defn -main [& args]
  (println args)
  (println (apply parse-cmd-line args)))


