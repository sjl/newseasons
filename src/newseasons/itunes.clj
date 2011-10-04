(ns newseasons.itunes
  (:require [clj-http.client :as client]) 
  (:use [cheshire.core :only (parse-string)]))


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


