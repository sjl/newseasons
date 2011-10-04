(defproject newseasons "0.1.0-SNAPSHOT"
            :description "FIXME: write this!"
            :dependencies [[org.clojure/clojure "1.2.1"]
                           [org.clojure/clojure-contrib "1.2.0"]
                           [noir "1.2.0"]
                           [postmark "1.0.0"]
                           [cheshire "2.0.2"]
                           [clj-http "0.2.1"]
                           [aleph "0.2.0-beta2"]]
            :main newseasons.server
            :run-aliases {:refresh newseasons.loops.refresh/main})

