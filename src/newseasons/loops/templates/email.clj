(ns newseasons.loops.templates.email)


(defn new-season [email show]
  (str "A new season of " (:title show) " has just hit iTunes!"
       "\n\n"
       (:latest show)
       "\n"
       (:url show)
       "\n\n"
       "If you want to stop receiving these emails you can delete your account "
       "by going to: http://newseasons.stevelosh.com/delete-account"))
