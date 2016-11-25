(ns cs-data-scrape.db
  (:require 
   [cs-data-scrape.files :as files]
   [cs-data-scrape.data :as data]
   [clojure.java.jdbc :as j]
   [clj-time.jdbc])
  (:gen-class))

(let [db-conf "database_info.xml"
      db-host (files/read-xml db-conf :db_host)
      db-port (files/read-xml db-conf :db_port)
      db-name (files/read-xml db-conf :db_name)
      db-user (files/read-xml db-conf :db_user)
      db-password (files/read-xml db-conf :db_password)]
  (def heroku-db {:dbtype "postgresql"
                  :dbname db-name
                  :host db-host
                  :user db-user
                  :password db-password
                  :ssl true
                  :sslfactory "org.postgresql.ssl.NonValidatingFactory"}))

(defn get-match-ids []
  (j/query
   heroku-db
   ["select match_id from match"]))

(defn get-matches []
  (j/query
   heroku-db
   ["select * from match"]))

(defn get-matches-month [month]
  (j/query
   heroku-db
   [(str "select * from match where extract(month from match_date) = " month)]))

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

(comment
  (for [map map-result-m
        i (range)]
    (do
      (println map)
      (j/insert!
       trans-conn
       :match_map_result
       {:match_id id
        :side_id 1
        :map_num i
        :map_name (:map map)
        :team_score_map (:team1-score-map (:map-results map))})
      (j/insert!
       trans-conn
       :match_map_result
       {:match_id id
        :side_id 2
        :map_num i
        :map_name (:map map)
        :team_score_map (:team2-score-map (:map-results map))}))
    ))


(defn insert-match-result! [id result-m]
  (let [gen-info-m (:gen-info result-m)
        map-result-m (:map-results result-m)
        match-result (data/match-result-from-maps map-result-m)
        map-result-inserts
        (reduce 
         #(conj %1
                {:match_id id
                 :side_id 1
                 :map_num (:map-num %2)
                 :map_name (:map %2)
                 :team_score_map (:team1-score-map (:map-results %2))}
                {:match_id id
                 :side_id 2
                 :map_num (:map-num %2)
                 :map_name (:map %2)
                 :team_score_map (:team2-score-map (:map-results %2))})
         []
         map-result-m)
        map-half-inserts
        (reduce 
         #(conj %1
                {:match_id id
                 :side_id 1
                 :map_num (:map-num %2)
                 :half_id 1
                 :half_side (:team1-side-r1 (:map-results %2))
                 :team_score_half (:team1-score-r1 (:map-results %2))}
                {:match_id id
                 :side_id 1
                 :map_num (:map-num %2)
                 :half_id 2
                 :half_side (:team1-side-r2 (:map-results %2))
                 :team_score_half (:team1-score-r2 (:map-results %2))}
                {:match_id id
                 :side_id 2
                 :map_num (:map-num %2)
                 :half_id 1
                 :half_side (:team2-side-r1 (:map-results %2))
                 :team_score_half (:team2-score-r1 (:map-results %2))}
                {:match_id id
                 :side_id 2
                 :map_num (:map-num %2)
                 :half_id 2
                 :half_side (:team2-side-r2 (:map-results %2))
                 :team_score_half (:team2-score-r2 (:map-results %2))}
                
                )
         []
         ;no halfs that are not played
         (filter :map-results map-result-m))
        votes (data/votes-formatted result-m)
        votes-inserts
        (reduce
         #(conj 
           %1
           {:match_id id
            :vote_id (:vote-id %2)
            :map_name (:map-name %2)
            :voter_name (:voter-name %2)
            :action (:action %2)
            :raw (:raw %2)})
         []
         votes)]
      (j/with-db-transaction [trans-conn heroku-db]
        ;match_result table
        (j/insert!
         trans-conn
         :match_result
         {:match_id id
          :side_id 1
          :team_name (:team1-name gen-info-m)
          :team_score (:team1-score match-result)
          :team_result (:team1-result gen-info-m)})
        (j/insert!
         trans-conn
         :match_result
         {:match_id id
          :side_id 2
          :team_name (:team2-name gen-info-m)
          :team_score (:team2-score match-result)
          :team_result (:team2-result gen-info-m)})
        ;match_map_result table
        (j/insert-multi!
         trans-conn
         :match_map_result
         map-result-inserts)
        ;match_map_half_result table
        (j/insert-multi!
         trans-conn
         :match_map_half_result
         map-half-inserts)
        ;match_vote table
        (j/insert-multi!
         trans-conn
         :match_vote
         votes-inserts))))


