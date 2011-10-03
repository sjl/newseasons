(ns newseasons.middleware.session.redis
  "Redis session storage."
  (:use ring.middleware.session.store)
  (:use [cheshire.core :only (generate-string parse-string)])
  (:use [aleph.redis :only (redis-client)])
  (:import java.util.UUID))


(deftype RedisStore [r]
  SessionStore

  (read-session [_ key]
    (parse-string (or @(r [:get key]) "{}")))

  (write-session [_ key data]
    (let [key (or key (str (UUID/randomUUID)))]
      @(r [:set key (generate-string data)])
      (r [:expire key (* 60 60 24 30)])
      key))

  (delete-session [_ key]
    @(r [:del key])
    nil))

(defn redis-store
  "Creates a Redis-backed storage engine."
  [host password] (RedisStore. (redis-client {:host host :password password})))


