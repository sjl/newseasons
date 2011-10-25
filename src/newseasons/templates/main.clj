(ns newseasons.templates.main
  (:require [noir.session :as sess])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers))


(def ga "
  <script type=\"text/javascript\">

        var _gaq = _gaq || [];
        _gaq.push(['_setAccount', 'UA-15328874-7']);
        _gaq.push(['_trackPageview']);

        (function() {
        var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
        var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
        })();

  </script>")

(def gauges "
  <script type='text/javascript'>
      var _gauges = _gauges || [];
      (function() {
        var t   = document.createElement('script');
        t.type  = 'text/javascript';
        t.async = true;
        t.id    = 'gauges-tracker';
        t.setAttribute('data-site-id', '4ea612e7613f5d4208000001');
        t.src = '//secure.gaug.es/track.js';
        var s = document.getElementsByTagName('script')[0];
        s.parentNode.insertBefore(t, s);
      })();
  </script>")


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
(defpartial base [body-class & content]
  (html5
    [:head
     (map include-css ["/css/base.css"
                       "/css/skeleton.css"
                       "/css/layout.css"])
     (include-less "/css/style.less")
     (include-js "/js/less.js")
     [:title "New Seasons"]]
    [:body {:class body-class}
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
        ". "
        "It&apos;s "
        (link-to "http://github.com/sjl/newseasons" "open source")
        "."]]]
     ga
     gauges]))

(defpartial inner [title body-class & content]
  (base body-class
        [:h2.sixteen.columns.page-title
         [:div.profile
          (form-to [:post "/logout"]
                   (submit-button "Log Out"))
          (form-to [:get "/password"]
                   (submit-button "Change Password"))]
         title]
        content
        (when-not (= body-class "user")
          [:p.sixteen.columns (link-to "/" "&larr; Back to Dashboard")]) 
        [:p.delete.sixteen.columns
         "Don't want to receive any more notifications? "
         "You can "
         (link-to "/delete-account" "delete your account")
         "."]))


; Pages -----------------------------------------------------------------------
(defpartial home []
  (base "home"
        [:div.six.columns
         (form-to [:post ""]
                  (field text-field "email" "Email Address")
                  (field password-field "password" "Password")
                  (submit-button "Log In or Create Account"))]
        [:div.five.columns
         [:p "New Seasons will notify you when your favorite TV "
          "shows have new seasons on iTunes."]]
        [:div.five.columns
         [:p "Add some shows to your list, we'll email you when new seasons come out.  That's it."]]))


(defpartial user-show [show]
  [:li.show.group
   (form-to [:post "/rem"]
            [:input {:type "hidden" :name "artist-id" :value (show :id)}]
            (submit-button "Remove"))
   [:img {:src (show :image)}]
   [:h3 (link-to (show :url) (show :title))]
   [:p.latest "Latest: " (show :latest)]])

(defpartial user [user]
  (inner (str "Hello, " (:email user)) "user"
         [:div.seven.columns
          (form-to [:get "search"]
                   (field text-field
                          "query"
                          "Which show do you want to keep track of?")
                   (submit-button "Search"))]
         [:div.nine.columns
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
                [:ul.shows (map user-show shows)])))]))


(defpartial result [r]
  [:li.show.group
   [:img {:src (r "artworkUrl100")}]
   [:h3 (link-to (r "artistViewUrl") (r "artistName"))]
   [:p.latest "Latest: " (r "collectionName")]
   (form-to [:post "/add"]
            [:input {:type "hidden" :name "artist-id" :value (r "artistId")}]
            (submit-button "Add Show to List"))])

(defpartial search [query results]
  (inner (str (if (empty? results)
                "No"
                (count results))
           " Result" (pluralize results)
           " for &ldquo;" query "&rdquo;")
    "search"
    [:ul.sixteen.columns.search-results
     (map result results)]))


(defpartial password []
  (inner "Change Your Password" "change-password"
         [:section.sixteen.columns
          (form-to [:post ""]
                   (field password-field "password" "New Password")
                   (submit-button "Change Password"))]))

(defpartial delete-account []
  (inner "Delete Your Account" "delete-account"
         [:section.sixteen.columns
          [:p "If you're sure about this you can delete your account here."]
          (form-to [:post ""]
                   (submit-button "Delete Account"))]))
