(ns cs-data-scrape.db
  (:require [clojure.java.jdbc :as j]
            [clj-time.jdbc])
  (:gen-class))

(def heroku-db {:dbtype "postgresql"
                :dbname "d5vdctaur9mfbq"
                :host "ec2-54-217-208-206.eu-west-1.compute.amazonaws.com"
                :user "FIXME"
                :password "FIXME"
                :ssl true
                :sslfactory "org.postgresql.ssl.NonValidatingFactory"})

(defn get-match-ids []
  (j/query
   heroku-db
   ["select match_id from match"]))

(defn get-matches []
  (j/query
   heroku-db
   ["select * from match"]))

(defn get-match-result-ids []
  (j/query
   heroku-db
   ["select match_id from match_result"]))

(defn insert-match! [id link date]
  (j/insert!
   heroku-db
   :match
   {:match_id id
    :link link
    :match_date date}))

(defn insert-match-result! [id result-m]
  (let [gen-info-m (:gen-info result-m)]
      (j/insert!
       heroku-db
       :match_result
       {:match_id id
        :team1_name (:team1-name gen-info-m)
        :team2_name (:team2-name gen-info-m)
        :team1_score (:team1-score gen-info-m)
        :team2_score (:team2-score gen-info-m)
        :team1_result (:team1-result gen-info-m)
        :team2_result (:team2-result gen-info-m)})))
