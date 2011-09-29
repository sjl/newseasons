(ns newseasons.views.main
  (:require [newseasons.templates.main :as t])
  (:use noir.core)
  (:require [noir.response :as resp])
  (:require [noir.session :as sess])
  (:require [clj-http.client :as client])
  (:use [cheshire.core :only (parse-string)]))


(def email-regex #"[a-zA-Z0-9._+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}")

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
(defmacro login-required [& body]
  `(if-not (sess/get :email)
     (do
       (sess/flash-put! "Please log in to access that page!")
       (resp/redirect "/")) 
     ~@body))


; Home ------------------------------------------------------------------------
(defn check-login [{:keys [email password]}]
  true)

(defpage [:get "/"] []
         (if-let [email (sess/get :email)]
           (resp/redirect (str "/" email))
           (t/home)))

(defpage [:post "/"] {:as login}
         (if (check-login login)
           (resp/redirect (str "/" (:email login)))
           (t/home)))


; User ------------------------------------------------------------------------
(defpage [:get ["/:email" :email email-regex]] {:keys [email]}
         (login-required
           (t/user email)))


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
         (sess/flash-put! "Added a show to your list.")
         (resp/redirect "/"))



