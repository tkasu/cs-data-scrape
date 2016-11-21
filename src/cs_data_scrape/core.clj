(ns cs-data-scrape.core
  (:gen-class)
  (:require [cs-data-scrape.scrape :as scrape]
            [cs-data-scrape.db :as db]))

(defn save-matches-after-to-db! [iso-date]
  (let [matches (scrape/scrape-matches-after! iso-date)
        db-ids (reduce #(conj %1 (:id %2)) #{} (db/get-match-ids)) 
        test-match (first matches)
        filtered-matches (remove #(contains? db-ids (:id %)) matches)]
    (for [match filtered-matches]
      (db/insert-match! 
       (:id match) 
       (:link match)
       (:match-date match)))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (save-matches-after-to-db! "2016-11-16"))


;DEV AREA
(comment

  (def db-ids (db/get-match-ids))

  (def matches (scrape/scrape-matches-after! "2016-11-17"))

  matches

  (def t-match (first matches))

  db-ids

  (set db-ids)

  (def db-ids (reduce #(conj %1 (:id %2)) #{} (db/get-match-ids)))

   (remove #(contains? db-ids (:id %)) matches)

  (contains? db-ids (:id (first matches)))

  (scrape/scrape-match! (:link (first (db/get-matches))))

)



