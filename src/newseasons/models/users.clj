(ns newseasons.models.users
  (:use newseasons.models.keys)
  (:use newseasons.utils)
  (:use [newseasons.models.shows :only (show-get)])
  (:require [noir.util.crypt :as crypt])
  (:use [aleph.redis :only (redis-client)]))


(def r (redis-client {:host "localhost" :password "devpass"}))

; "Schema" --------------------------------------------------------------------
;
; Users are stored as Redis hashes, with their watched shows as a separate set.
;
; user:<email address> = {
;     email: the email address for ease of use
;     pass: the user's hashed password
; }
; user:<email address>:shows = #{show-id, ...}
;
; The show watching data is also denormalized based on show.
;
; shows:<id>:watchers - #{email, ...}

; Code ------------------------------------------------------------------------
(defn user-get [email]
  (let [user (apply hash-map @(r [:hgetall (key-user email)]))]
    (when (not (empty? user))
      (merge {:email (user "email") :pass (user "pass")}
             {:shows (sort-maps-by (map show-get
                                        @(r [:smembers (key-user-shows email)]))
                                   :title)}))))

(defn user-set-email! [email new-email]
  @(r [:hset (key-user email) "email" new-email]))

(defn user-set-pass! [email new-pass]
  @(r [:hset (key-user email) "pass" (crypt/encrypt new-pass)]))

(defn user-add-show! [email show-id]
  @(r [:sadd (key-user-shows email) show-id])
  @(r [:sadd (key-show-watchers show-id) email]))

(defn user-rem-show! [email show-id]
  @(r [:srem (key-user-shows email) show-id])
  @(r [:srem (key-show-watchers show-id) email]))
