(ns cs-data-scrape.core
  (:gen-class)
  (:require [cs-data-scrape.scrape :as scrape]
            [cs-data-scrape.db :as db]
            [cs-data-scrape.data :as data]))

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
  (save-matches-after-to-db! "2016-06-30"))

(+ 4 4)


;DEV AREA

(comment

  (def db-ids (db/get-match-ids))

  (def matches (db/get-matches))

  (def matches (scrape/scrape-matches-after! "2016-11-22"))

  matches
  (println matches)

  (count matches)

  (def t-match (nth matches 5))

  (def vote-test-m (first (filter #(= (:match_id %) "2306898-aura-parallax-cybergamer-pro-league-season-9") matches)))

(data/votes-formatted vote-m-data)

  vote-test-m

  (:link vote-test-m)

  (:link t-match)

  (def match-data (scrape/scrape-match! (:link t-match)))

 (def vote-m-data (scrape/scrape-match! (:link vote-test-m)))

 vote-m-data

 (:votes vote-m-data)

 (def t-vote (nth (:votes vote-m-data) 4))

t-vote

(def test-str "123 lololo 234")

(re-find #"[0-9]+" test-str)


{:vote_id (re-find #"[0-9]+" t-vote)
 :action (let [pick (re-find #"pick" t-vote)
               remove (re-find #"remove" t-vote)
               leftover (re-find #"left over" t-vote)
               random (re-find #"random" t-vote)]
           (cond
            (not (nil? pick)) "pick"
            (not (nil? remove)) "remove"
            (not (nil? leftover)) "leftover"
            (not (nil? random)) random
            :else nil))
 :raw t-vote}

(:team2-name (:gen-info vote-m-data))

(re-find (re-pattern (:team2-name (:gen-info vote-m-data))) t-vote)

(conj
 {}
 (:map (first (:map-results vote-m-data))))

(def maps (reduce #(conj % (:map %2)) #{} (:map-results vote-m-data)))

maps

(let
    [maps (reduce #(conj % (:map %2)) #{} (:map-results vote-m-data))
     map-pattern
     (->
      (reduce #(str %1 %2 "|") "(?i)(" maps)
      (str ")")
      ;Remove last "|"
      (clojure.string/replace-first
       (re-pattern "\\|\\)")
       "\\)")
      (re-pattern))]
  (first (re-find map-pattern t-vote)))

(str (drop-last "abcd"))

(clojure.string/replace-last "abcd" #"d" "")

(first (re-find #"(?i)(dust|mirage|train)" t-vote))

(first nil)

t-vote



(let [votes (:votes vote-m-data)
      team1-name (:team1-name (:gen-info vote-m-data))
      team2-name (:team2-name (:gen-info vote-m-data))
      map-s (reduce #(conj % (:map %2)) #{} (:map-results vote-m-data))
     map-pattern
     (->
      (reduce #(str %1 %2 "|") "(?i)(" map-s)
      (str ")")
      ;Remove last "|"
      (clojure.string/replace-first
       (re-pattern "\\|\\)")
       "\\)")
      (re-pattern))]
    (reduce 
     #(conj %1 {:vote-id (re-find #"[0-9]+" %2)
                :map-name (let [parsed-map (first (re-find map-pattern %2))]
                              (if parsed-map
                                  (map-corrected-case parsed-map map-s)
                                  nil))
                :voter-name (let [team1-pick (re-find (re-pattern team1-name) %2)
                                  team2-pick (re-find (re-pattern team2-name) %2)]
                              (cond
                               (not (nil? team1-pick)) team1-pick
                               (not (nil? team2-pick)) team2-pick
                               :else nil))
                :action (let [pick (re-find #"pick" %2)
                              remove (re-find #"remove"%2)
                              leftover (re-find #"left over" %2)
                              random (re-find #"random" %2)]
                          (cond
                           (not (nil? pick)) "pick"
                           (not (nil? remove)) "remove"
                           (not (nil? leftover)) "leftover"
                           (not (nil? random)) "random"
                           :else nil))
                :raw %2})
     []
     votes))

 maps

(first ())

(clojure.string/upper-case "lol2")

(map-corrected-case nil maps)

(defn map-corrected-case [map-name maps]
  (first
   (filter #(= (clojure.string/upper-case %) (clojure.string/upper-case map-name)) maps)))

(get-map-corrected-case "DUST2" maps)

  match-data

  (:map-results match-data)
  (reduce 
   #_#(conj %1 (:team1-score-map (:map-results %2)))  #(+ %1 (:team1-score-map (:map-results %2))) 

(reduce 
   #(cond
     (>(:team1-score-map (:map-results %2)) (:team2-score-map (:map-results %2)))
     {:team1-score (inc (:team1-score %1))
      :team2-score (:team2-score %1)}
     (< (:team1-score-map (:map-results %2)) (:team2-score-map (:map-results %2)))
     {:team1-score (:team1-score %1)
      :team2-score (inc (:team2-score %1))}
     :else %2) 
   {:team1-score 0 
    :team2-score 0}
   (filter :map-results (:map-results match-data)))

  (db/match-result-from-maps (:map-results match-data))

  (filter :map-results (:map-results match-data))

  (db/insert-match-result! (:id t-match) match-data)

  (defn insert-non-inserted-matches! []
    (let [scraped-match-links (db/get-matches)
          scraped-result-ids (reduce #(conj %1 (:match_id %2)) #{} (db/get-match-result-ids))
          matches-to-scrape (remove #(contains? scraped-result-ids (:match_id %)) scraped-match-links)]
      (for [match matches-to-scrape
            :let [match-result-m (scrape/scrape-match! (:link match))]]
        (db/insert-match-result!
         (:match_id match)
         match-result-m))))

  (insert-non-inserted-matches!)

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
    "2306655-chiefs-syf-esea-premier-season-23-oceania" ;forfeit
    }
  )

(count non-valid-match-ids)

(insert-non-inserted-matches-from-month! 11)

(def nov-matches (db/get-matches-month 11))

(count nov-matches)

  match-data

  db-ids

  (set db-ids)

  (def db-ids (reduce #(conj %1 (:id %2)) #{} (db/get-match-ids)))

   (remove #(contains? db-ids (:id %)) matches)

  (contains? db-ids (:id (first matches)))

  (scrape/scrape-match! (:link (first (db/get-matches))))

))


