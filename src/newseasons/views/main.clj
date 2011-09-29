(ns newseasons.views.main
  (:use noir.core)
  (:require [noir.response :as resp])
  (:require [noir.session :as sess])
  (:require [noir.util.crypt :as crypt])
  (:require [clj-http.client :as client])
  (:use [cheshire.core :only (parse-string)])
  (:require [newseasons.templates.main :as t])
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
(defpage [:get "/search"] {:keys [query]}
         (login-required
           ; TODO: Images.
           (let [results (itunes-search-show query)
                 artists (distinct (map #(select-keys % ["artistName" "artistId" "artistViewUrl"])
                                        results))]
             (t/search query artists))))


; Add -------------------------------------------------------------------------
(defpage [:post "/add"] {:as show}
         (login-required
           (flash! "Added a show to your list.")
           (resp/redirect "/")))


; Log Out ---------------------------------------------------------------------
(defpage [:post "/logout"] []
         (sess/remove! :email)
         (resp/redirect "/"))
