(ns newseasons.models.keys)

(defn key-show [id]
  (str "shows:" id))

(defn key-user [email]
  (str "users:" email))

(defn key-user-shows [email]
  (str "users:" email ":shows"))

(defn key-show-watchers [id]
  (str "shows:" id ":watchers"))
