(ns newseasons.models.shows
  (:use newseasons.models.keys)
  (:use [aleph.redis :only (redis-client)]))


(def r (redis-client {:host "localhost" :password "devpass"}))

; "Schema" --------------------------------------------------------------------
;
; Shows are stored as hashes.
;
; shows:<iTunes artist ID> = {
;     id: show id
;     title: show tile
;     latest: description of the latest season
;     image: url to show's image
;     url: url to view the show on iTunes
; }

; Code ------------------------------------------------------------------------

(defn show-get [id]
  (let [show (apply hash-map @(r [:hgetall (key-show id)]))]
    (when (not (empty? show))
      {:id (show "id")
       :title (show "title")
       :image (show "image")
       :latest (show "latest")
       :url (show "url")})))

(defn show-set-id! [id new-id]
  @(r [:hset (key-show id) "id" new-id]))

(defn show-set-title! [id new-title]
  @(r [:hset (key-show id) "title" new-title]))

(defn show-set-latest! [id new-latest]
  @(r [:hset (key-show id) "latest" new-latest]))

(defn show-set-image! [id new-image]
  @(r [:hset (key-show id) "image" new-image]))

(defn show-set-url! [id new-url]
  @(r [:hset (key-show id) "url" new-url]))

