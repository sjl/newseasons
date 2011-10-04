(ns newseasons.loops.refresh
  (:require [newseasons.models.shows :as shows])
  (:require [newseasons.itunes :as itunes])
  )

(defn- refresh-show [id]
  (println "  refreshing" id)
  (let [show (itunes/itunes-lookup-seasons id)]
    (println show)
    (println "    ->" (show "artistName"))
    (Thread/sleep 5000)))

(defn- refresh []
  (println "Refreshing Shows")
  (let [shows (shows/shows-get-to-check)]
    (dorun (map refresh-show shows))))


(defn main [& args]
  (println "Starting Refresh Loop!")
  (println)
  (dorun (repeatedly refresh)))
