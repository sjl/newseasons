(ns newseasons.templates.main
  (:require [noir.session :as sess])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers))


; Utils -----------------------------------------------------------------------
(defn include-less [href]
  [:link {:rel "stylesheet/less" :type "text/css" :href href}])

(defn field [fieldfn field-name label]
  (list [:label {:for (str "id_" field-name)} label]
        (fieldfn field-name)))

(defn pluralize
  ([coll] (pluralize coll "s"))
  ([coll plural-suffix] (if (= 1 (count coll))
                         ""
                         plural-suffix)))


; Layout ----------------------------------------------------------------------
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
                (when-let [message (sess/flash-get)]
                  [:section.message.sixteen.columns
                   [:p message]])
                content
                [:footer.sixteen.columns
                 [:p
                  "Made by "
                  (link-to "http://stevelosh.com/" "Steve Losh")
                  " with "
                  (link-to "http://webnoir.org/" "Noir")
                  "."]]]]))

(defpartial inner [title & content]
            (base
              [:h2.sixteen.columns.page-title
               [:div.profile
                (form-to [:post "/logout"]
                         (submit-button "Log Out"))
                (form-to [:get "/password"]
                         (submit-button "Change Password"))]
               title]
              content))


; Pages -----------------------------------------------------------------------
(defpartial home []
            (base
              [:div.six.columns
               [:form {:action "" :method "POST"}
                (field text-field "email" "Email Address")
                (field password-field "password" "Password")
                (submit-button "Log In or Create Account")]]
              [:div.five.columns
               [:p "New Seasons will notify you when your favorite TV "
                   "shows have new seasons on iTunes.  That's it."]]
              [:div.five.columns
               [:p "New Seasons will notify you when your favorite TV "
                   "shows have new seasons on iTunes.  That's it."]]))


(defpartial user-show [show]
            [:li.show.group
             [:img {:src (show :image)}]
             [:h3 (link-to (show :url) (show :title))]
             (form-to [:post "/rem"]
                      [:input {:type "hidden" :name "artist-id" :value (show :id)}]
                      (submit-button "Remove"))])

(defpartial user [user]
            (inner (str "Hello, " (:email user))
                   [:div.eight.columns
                    [:form {:action "/search"}
                     (field text-field
                            "query"
                            "Which show do you want to keep track of?")
                     (submit-button "Search")]]
                   [:div.eight.columns
                    (let [shows (:shows user)]
                      (if (empty? shows)
                        [:p "You're not currently watching any shows."]
                        (list
                          [:p
                           "You're watching "
                           (count shows)
                           " show"
                           (pluralize shows)
                           "."]
                          [:ul (map user-show shows)])))]))


(defpartial result [r]
            [:li.show.group
             [:img {:src (r "artworkUrl100")}]
             [:h3 (link-to (r "artistViewUrl") (r "artistName"))]
             (form-to [:post "/add"]
                      [:input {:type "hidden" :name "artist-id" :value (r "artistId")}]
                      (submit-button "Add Show to List"))])

(defpartial search [query results]
            (inner (str "Search results for &ldquo;" query "&rdquo;")
                   [:ul.sixteen.columns.search-results
                    (map result results)]))


(defpartial password []
            (inner "Change Your Password"
                   [:section.sixteen.columns
                    (form-to [:post ""]
                             (field password-field "password" "New Password")
                             (submit-button "Change Password"))]))
