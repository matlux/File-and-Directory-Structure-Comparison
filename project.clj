(defproject cl-file-comparator "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [
                 [org.clojure/clojure "1.4.0"]
                 [clj-ssh "0.3.0"]
                 [org.clojure/tools.cli "0.2.1"]
                 [slacker "0.4.0"]
                 ]
  :dev-dependencies [[lein-eclipse "1.0.0"]]
  :main net.matlux.filecomparator.app
)
