(ns newseasons.views.main
  (:use noir.core)
  (:require [noir.response :as resp])
  (:require [noir.session :as sess])
  (:require [noir.util.crypt :as crypt])
  (:require [clj-http.client :as client])
  (:use [cheshire.core :only (parse-string)])
  (:require [newseasons.templates.main :as t])
  (:require [newseasons.models.shows :as shows])
  (:require [newseasons.models.users :as users]))


; Utils -----------------------------------------------------------------------
(def email-regex #"[a-zA-Z0-9._+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}")
(def none-are (comp not some))
(defn all-are [pred coll]
  (= (count coll)
     (count (filter pred coll))))

(defn flash! [message]
  (sess/flash-put! message)
  nil)


; Utils -----------------------------------------------------------------------
(defn unique-by
  "Turn a sequence of maps into a new sequence with duplicated removed, with
  uniqueness determined by the given keys.
  
  Ex:

      (def a {:foo 1 :bar 1 :baz 1})
      (def b {:foo 1 :bar 1 :baz 2})
      (def c {:foo 1 :bar 2 :baz 3})

      (unique-by [a b c] :foo)
      ;=> [c]
      (unique-by [a b c] :foo :bar)
      ;=> [b c]
      (unique-by [a b c] :baz)
      ;=> [a b c]
  "
  [coll & ks]
  (vals (reduce merge {} (map #(vector (select-keys % ks) %) coll))))

(defn unique-shows [seasons]
  (unique-by seasons "artistId"))

(defn sort-maps-by [coll k]
  (sort #(compare (%1 k) (%2 k)) coll))


; iTunes ----------------------------------------------------------------------
(defn itunes-search [params]
  ((parse-string (:body (client/get "http://itunes.apple.com/search"
                                    {:query-params params})))
     "results"))

(defn itunes-search-show [query]
  (itunes-search {"term" query
                  "media" "tvShow"
                  "entity" "tvSeason"
                  "attribute" "showTerm"}))


(defn itunes-lookup [field id]
  ((parse-string (:body (client/get "http://itunes.apple.com/search"
                                    {:query-params {field id}})))
     "results"))

(defn itunes-lookup-artist [id]
  (first (itunes-lookup "id" id)))


; Authentication --------------------------------------------------------------
(defn force-login []
  (flash! "Please log in to view that page!")
  (resp/redirect "/"))

(defmacro login-required [& body]
  `(if-not (sess/get :email)
     (force-login)
     (do ~@body)))

(defn check-login [{:keys [email password]}]
  (if-not (none-are empty? [email password])
    (flash! "Both fields are required.  This really shouldn't be difficult.")
    (if-not (re-find email-regex email)
      (flash! "That's not an email address!")
      (if-let [user (users/user-get email)]
        (if (crypt/compare password (:pass user))
          (do
            (sess/put! :email email)
            user)
          (flash! "Invalid login!"))
        (do
          (users/user-set-email! email email)
          (users/user-set-pass! email password)
          (sess/put! :email email)
          (users/user-get email))))))


; Home ------------------------------------------------------------------------
(defpage [:get "/"] []
         (if-let [email (sess/get :email)]
           (resp/redirect (str "/" email))
           (t/home)))

(defpage [:post "/"] {:as login}
         (if (check-login login)
           (resp/redirect (str "/" (:email login)))
           (render "/" login)))


; User ------------------------------------------------------------------------
(defpage [:get ["/:email" :email email-regex]] {:keys [email]}
         (login-required
           (if (not= email (sess/get :email))
             (force-login)
             (t/user email))))


; Search ----------------------------------------------------------------------
(defn store-show [show]
  (let [id (show "artistId")]
    (shows/show-set-title! id (show "artistName"))
    (shows/show-set-image! id (show "artworkUrl100"))
    (shows/show-set-url! id (show "artistViewUrl"))))

(defn store-shows [seasons]
  (dorun (map store-show seasons)))


(defpage [:get "/search"] {:keys [query]}
         (login-required
           (let [results (unique-shows (itunes-search-show query))]
             (store-shows (sort-maps-by results "releaseDate"))
             (t/search query results))))


; Add -------------------------------------------------------------------------
(defpage [:post "/add"] {:as show}
         (login-required
           (flash! "Added a show to your list.")
           (resp/redirect "/")))


; Change Password -------------------------------------------------------------
(defpage [:get "/password"] []
         (login-required
           (t/password)))


(defn- pass-valid? [password]
  (not (empty? password)))

(defpage [:post "/password"] {:keys [password]}
         (login-required
           (if-not (pass-valid? password)
             (render "/password" password)
             (do
               (users/user-set-pass! (sess/get :email) password)
               (resp/redirect "/")))))


; Log Out ---------------------------------------------------------------------
(defpage [:post "/logout"] []
         (sess/remove! :email)
         (resp/redirect "/"))
