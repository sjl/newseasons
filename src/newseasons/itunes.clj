(ns newseasons.itunes
  (:require [clj-http.client :as client]) 
  (:use newseasons.utils)
  (:use [cheshire.core :only (parse-string)]))


(defn itunes-search [params]
  (try
    ((parse-string (:body (client/get "http://itunes.apple.com/search"
                                      {:query-params params})))
       "results")
    (catch java.lang.Exception e
      (println "Failed request to " (:url (.data e))))))

(defn itunes-search-show [query]
  (itunes-search {"term" query
                  "media" "tvShow"
                  "entity" "tvSeason"
                  "attribute" "showTerm"}))


(defn itunes-lookup [field id entity]
  (try
    ((parse-string (:body (client/get "http://itunes.apple.com/lookup"
                                      {:query-params {field id
                                                      "entity" entity}})))
       "results")
    (catch java.lang.Exception e
      (println "Failed request to " (:url (.data e))))))

(defn itunes-lookup-seasons [id]
  (let [results (itunes-lookup "id" id "tvSeason")]
    (last (sort-maps-by (filter #(% "collectionName")
                                results)
                        "releaseDate"))))


