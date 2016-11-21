(ns cs-data-scrape.db
  (:require [clojure.java.jdbc :as j]
            [clj-time.jdbc])
  (:gen-class))

(def heroku-db {:dbtype "postgresql"
                :dbname "FIXME"
                :host "FIXME"
                :user "FIXME"
                :password "FIXME"
                :ssl true
                :sslfactory "org.postgresql.ssl.NonValidatingFactory"})

(defn get-match-ids []
  (j/query
   heroku-db
   ["select id from match"]))

(defn get-matches []
  (j/query
   heroku-db
   ["select * from match"]))

(defn insert-match! [id link date]
  (j/insert!
   heroku-db
   :match
   {:id id
    :link link
    :match_date date}))
