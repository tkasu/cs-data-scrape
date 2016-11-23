(ns cs-data-scrape.core
  (:gen-class)
  (:require [cs-data-scrape.scrape :as scrape]
            [cs-data-scrape.db :as db]))

(defn save-matches-after-to-db! [iso-date]
  (let [matches (scrape/scrape-matches-after! iso-date)
        db-ids (reduce #(conj %1 (:id %2)) #{} (db/get-match-ids))
        test-match (first matches)
        filtered-matches (remove #(contains? db-ids (:match_id %)) matches)]
    (for [match filtered-matches]
      (db/insert-match! 
       (:id match) 
       (:link match)
       (:match-date match)))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (save-matches-after-to-db! "2016-11-22"))


;DEV AREA
(comment

  (def db-ids (db/get-match-ids))

  (def matches (db/get-matches))

  (def matches (scrape/scrape-matches-after! "2016-11-22"))

  matches

  (count matches)

  (def t-match (first matches))

  (:link t-match)

  (def match-data (scrape/scrape-match! (:link t-match)))

  match-data

  (:map-results match-data)

  (filter :map-results (:map-results match-data))

  (db/insert-match-result! (:id t-match) match-data)

  (defn insert-non-inserted-matches []
    (let [scraped-match-links (db/get-matches)
          scraped-result-ids (reduce #(conj %1 (:match_id %2)) #{} (db/get-match-result-ids))
          matches-to-scrape (remove #(contains? scraped-result-ids (:match_id %)) scraped-match-links)]
      (for [match matches-to-scrape
            :let [match-result-m (scrape/scrape-match! (:link match))]]
        (db/insert-match-result!
         (:match_id match)
         match-result-m))))

  (insert-non-inserted-matches)

  match-data

  db-ids

  (set db-ids)

  (def db-ids (reduce #(conj %1 (:id %2)) #{} (db/get-match-ids)))

   (remove #(contains? db-ids (:id %)) matches)

  (contains? db-ids (:id (first matches)))

  (scrape/scrape-match! (:link (first (db/get-matches))))

)



