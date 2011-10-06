(ns newseasons.loops.refresh
  (:require [newseasons.models.shows :as shows])
  (:require [newseasons.itunes :as itunes]))


; Dammit, Clojure.
(defn- gt [a b]
  (<= 1 (compare a b)))

(defn- lt [a b]
  (>= -1 (compare a b)))


(defn- notify [show email]
  (println "    to:" email)
  (println "        Sweet, a new season of" (:title show) "has been released!")
  (println "        New season:" (:latest show)))

(defn- notify-all [show-id]
  (let [show (shows/show-get show-id)
        watchers (shows/show-get-watchers show-id)]
    (dorun (map notify (cycle [show]) watchers))))

(defn- check-and-notify [show]
  (let [id (show "artistId")
        old-release-date (shows/show-get-version id)
        new-release-date (show "releaseDate")]
    (when (gt new-release-date
              old-release-date)
      (notify-all id)
      (shows/show-set-version! id new-release-date))))

(defn- refresh-show [id]
  (println "  refreshing" id)
  (let [show (itunes/itunes-lookup-seasons id)]
    (if show
      (do
        (check-and-notify show)
        (shows/store-raw-show show)
        (println (show "artistName") "/" (show "collectionName")))
      (println "(unknown)"))
    (Thread/sleep 4000)))

(defn- refresh []
  (println "")
  (println "Refreshing Shows")
  (println "----------------")

  (let [shows (shows/shows-get-to-check)]
    (dorun (map refresh-show shows))))


(defn main [& args]
  (println "Starting Refresh Loop!")
  (println "======================")
  (dorun (repeatedly refresh)))
