(ns newseasons.loops.refresh
  (:use [postmark.core :only (postmark)])
  (:use [newseasons.loops.templates.email :only (new-season)])
  (:use [newseasons.settings :only (postmark-api-key)])
  (:require [newseasons.models.shows :as shows])
  (:require [newseasons.itunes :as itunes]))

(def pm (postmark postmark-api-key "newseasons@stevelosh.com"))

; Dammit, Clojure.
(defn- gt [a b]
  (<= 1 (compare a b)))

(defn- lt [a b]
  (>= -1 (compare a b)))


(defn- notify [show email]
  (let [body (new-season email show)]
    (println (pm {:to email
                  :subject (str "[New Seasons] A new season of "
                                (:title show)
                                " has hit iTunes!")
                  :text body
                  :tag "newseasons"}))))

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
    (when show
      (shows/store-raw-show show)
      (check-and-notify show))
    (Thread/sleep 10000)))

(defn- refresh []
  (println "\n\n")
  (println "Refreshing Shows")
  (println "----------------")
  (Thread/sleep 20000)

  (let [shows (shows/shows-get-to-check)]
    (dorun (map refresh-show shows))))


(defn main [& args]
  (println "Starting Refresh Loop!")
  (println "======================")
  (dorun (repeatedly refresh)))
