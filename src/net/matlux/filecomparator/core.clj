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


(defn
  #^{:test (fn [] (let [hostname "192.168.77.14"
                        identity-path "/Users/mathieu/.ssh/id_rsa_no_pass"]
         (assert (= {:only-k2 #{}, :only-k1 #{}, :present-but-different ()}
                    (diff-targets {:user nil, :proto "local"
                              :hostname nil :port 22 :identity nil 
                              :path "treestructure/tree1"}
                                  {:user nil, :proto "local"
                              :hostname nil :port 22 :identity nil
                              :path "treestructure/tree1"})))
         (assert (= '{:only-k2 #{"/exampledir/file-only-in-2"},
                      :only-k1 #{"/file-only-in-1" "/example dir with spaces/fileonly-in-1"},
                      :present-but-different (("/file-dif-2" "c192af7146baeed80082973f4ebaa88f" "ded88e1100bee63f181b98a80e35d91b"))}
                    (diff-targets {:user nil, :proto "local"
                              :hostname nil :port 22 :identity nil 
                              :path "treestructure/tree1"}
                             {:user nil, :proto "local"
                              :hostname nil :port 22 :identity nil
                              :path "treestructure/tree2"})))
         (assert (= {:only-k2 #{}, :only-k1 #{}, :present-but-different ()}
                    (diff-targets {:user nil, :proto "ssh"
                           :hostname hostname :port 22 :identity identity-path
                           :path "/tmp/treestructure/tree1/"}
                          {:user nil, :proto "ssh"
                           :hostname hostname :port 22 :identity identity-path
                           :path "/tmp/treestructure/tree1/"})))
         (assert (= '{:only-k2 #{"exampledir/file-only-in-2"}, 
                      :only-k1 #{"file-only-in-1" "example dir with spaces/fileonly-in-1"},
                      :present-but-different (("file-dif-2" "c192af7146baeed80082973f4ebaa88f" "ded88e1100bee63f181b98a80e35d91b"))}
                    (diff-targets {:user nil, :proto "ssh"
                           :hostname hostname :port 22 :identity identity-path
                           :path "/tmp/treestructure/tree1/"}
                          {:user nil, :proto "ssh"
                           :hostname hostname :port 22 :identity identity-path
                           :path "/tmp/treestructure/tree2/"})))

         ))}

  diff-targets [target1 target2]
  (let [digested-files1 (digest-target target1)
        digested-files2 (digest-target target2)
        parsed-map (diff-set-dir-analysis digested-files1 digested-files2)]
    parsed-map)) 

(defn
  display-diff-targets [target1 target2]
  (let [parsed-map (diff-targets target1 target2)]
    (format-file-diff-set parsed-map ))) 

  
(comment
(diff-file-dir "treestructure/tree1/" "treestructure/tree2/")
)
  
