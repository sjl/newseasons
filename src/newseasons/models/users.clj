(ns newseasons.models.users
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
; user:<email address>:shows = #(show-id, ...)

; Code ------------------------------------------------------------------------
(defn- user-key [email]
  (str "users:" email))

(defn- user-key-shows [email]
  (str "users:" email ":shows"))

(defn user-get [email]
  (let [user @(r [:hgetall (user-key email)])]
    (when (not (empty? user))
      (merge user
             {:shows @(r [:smembers (user-key-shows email)])}))))

(defn user-set-email! [email new-email]
  @(r [:hset (user-key email) "email" new-email]))

(defn user-set-pass! [email new-pass]
  @(r [:hset (user-key email) "pass" (crypt/encrypt new-pass)]))

(defn user-add-show! [email show-id]
  @(r [:sadd (user-key-shows email) show-id]))

(defn user-rem-show! [email show-id]
  @(r [:srem (user-key-shows email) show-id]))
