(ns newseasons.models.shows
  (:use [aleph.redis :only (redis-client)]))


(def r (redis-client {:host "localhost" :password "devpass"}))

; "Schema" --------------------------------------------------------------------
;
; Shows are stored as hashes.
;
; shows:<iTunes artist ID> = {
;     title: show tile
;     image: url to show's image
;     url: url to view the show on iTunes
; }

; Code ------------------------------------------------------------------------
(defn- show-key [id]
  (str "shows:" id))


(defn show-get [id]
  (apply hash-map @(r [:hgetall (show-key id)])))

(defn show-set-title! [id new-title]
  @(r [:hset (show-key id) "title" new-title]))

(defn show-set-image! [id new-image]
  @(r [:hset (show-key id) "image" new-image]))

(defn show-set-url! [id new-url]
  @(r [:hset (show-key id) "url" new-url]))

