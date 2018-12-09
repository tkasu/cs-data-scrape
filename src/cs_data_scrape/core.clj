(ns cs-data-scrape.core
  (:gen-class)
  (:require [cs-data-scrape.scrape :as scrape]
            [cs-data-scrape.db :as db]
            [cs-data-scrape.data :as data]))

(def non-valid-match-ids
  #{"2306729-carbon-white-rabbit-esea-open-season-23-south-africa" ;forfeit
    "2306410-selfless-just-make-it-fast-americas-minor-eleague-major-2017-open-qualifier-1";JMiF couldn't field a full line-up and forfeited the match
    "2306450-gambit-kinguin-predator-masters-3" ;Grand final. Gambit will have a 1-0 map advantage due to coming from the upper bracket
    "2306743-nxl-mith-esea-open-season-23-asia" ;forfeit
    "2306479-pain-ronin-americas-minor-eleague-major-2017-open-qualifier-2" ;forfeit
    "2306381-revolte-guerilla-method-vitalbet-balkan-pro-league" ;forfeit
    "2306397-vgcyberzen-themongolz-mr-cat-masters-asian" ;Grand final. TheMongolz have an automatic 1-0 advantage due to coming from the upper bracket
    "2306382-guerilla-method-revolte-vitalbet-balkan-pro-league" ;forfeit
    "2306796-orange-brutality-esea-open-season-23-asia" ;forfeit
    "2306656-avant-garde-athletico-esea-premier-season-23-oceania" ;forfeit
    "2306633-progaming-3xteam-wca-2016-americas" ;?? forfeit ??
    "2306653-immunity-incept-esea-premier-season-23-oceania" ;forfeit
    "2306655-chiefs-syf-esea-premier-season-23-oceania"}) ;forfeit



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

(defn insert-non-inserted-matches-from-month! [month]
  (let [scraped-match-links (db/get-matches-month month)
        scraped-result-ids (reduce #(conj %1 (:match_id %2)) #{} (db/get-match-result-ids))
        matches-wo-already-scraped (remove #(contains? scraped-result-ids (:match_id %)) scraped-match-links)
        matches-to-scrape (remove #(contains? non-valid-match-ids (:match_id %)) matches-wo-already-scraped)]
    (for [match matches-to-scrape
          :let [match-result-m (scrape/scrape-match! (:link match))]]
      (db/insert-match-result!
        (:match_id match)
        match-result-m))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (save-matches-after-to-db! "2016-06-30")
  #_(insert-non-inserted-matches-from-month! 11))
