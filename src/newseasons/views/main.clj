(ns newseasons.views.main
  (:require [newseasons.templates.main :as t])
  (:use noir.core)
  (:require [noir.response :as resp])
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


; Home ------------------------------------------------------------------------
(defpage [:get "/"] []
         (t/home))

(defpage [:post "/"] []
         (t/home))


; User ------------------------------------------------------------------------
(defpage [:get ["/:email" :email email-regex]] {:keys [email]}
         (t/user email))


; Search ----------------------------------------------------------------------

(defpage [:get "/search"] {:keys [query]}
         ; TODO: Images.
         (let [results (itunes-search-show query)
               artists (distinct (map #(select-keys % ["artistName" "artistId" "artistViewUrl"])
                                      results))]
           (t/search query artists)))


; Add -------------------------------------------------------------------------
(defpage [:post "/add"] {:as show}
         (resp/redirect "/"))
