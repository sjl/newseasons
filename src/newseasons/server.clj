(ns newseasons.server
  (:require [noir.server :as server]))

(server/load-views "src/newseasons/views/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8000"))]
    (server/start port {:mode mode
                        :ns 'newseasons})))

