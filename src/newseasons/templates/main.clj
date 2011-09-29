(ns newseasons.templates.main
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers))

(defn include-less [href]
  [:link {:rel "stylesheet/less" :type "text/css" :href href}])

(defn field [fieldfn field-name label]
  (list [:label {:for (str "id_" field-name)} label]
        (fieldfn field-name)))


(defpartial base [& content]
            (html5
              [:head
               (map include-css ["/css/base.css"
                                 "/css/skeleton.css"
                                 "/css/layout.css"])
               (include-less "/css/style.less")
               (include-js "/js/less.js")
               [:title "New Seasons"]]
              [:body
               [:div.container.clearfix
                [:header.sixteen.columns [:h1 (link-to "/" "New Seasons")]]
                content
                [:footer.sixteen.columns
                 [:p
                  "Made by "
                  (link-to "http://stevelosh.com" "Steve Losh")
                  "."]]]]))

(defpartial inner [title & content]
            (base
              [:h2.sixteen.columns title]
              content))

(defpartial home []
            (base
              [:div.six.columns
               [:form {:action "" :method "POST"}
                (field text-field "email" "Email Address")
                (field password-field "password" "Password")
                (submit-button "Log in or Create Account")]]
              [:div.five.columns
               [:p "New Seasons will notify you when your favorite TV "
                   "shows have new seasons on iTunes.  That's it."]]
              [:div.five.columns
               [:p "New Seasons will notify you when your favorite TV "
                   "shows have new seasons on iTunes.  That's it."]]))

(defpartial user [email]
            (inner (str "Hello, " email)
                   [:div.eight.columns
                    [:form {:action "/search"}
                     (field text-field
                            "query"
                            "Which show do you want to keep track of?")
                     (submit-button "Search")]]
                   [:div.eight.columns
                    [:p "You're not currently waiting for any shows."]]))

(defpartial result [r]
            [:li
             [:h3 (link-to (r "artistViewUrl") (r "artistName"))]
             (form-to [:post "/add"]
                      [:input {:type "hidden" :name "artist-id" :value (r "artistId")}] 
                      (submit-button "Watch for this Show"))])

(defpartial search [query results]
            (inner (str "Search results for &ldquo;" query "&rdquo;")
                   [:ul.sixteen.columns
                    (map result results)]))
