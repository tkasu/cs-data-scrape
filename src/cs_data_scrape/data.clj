(ns cs-data-scrape.data
  (:gen-class))

(defn match-result-from-maps [map-result-m]
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
  (filter :map-results map-result-m)))

(defn map-corrected-case [map-name maps]
  (first
   (filter #(= (clojure.string/upper-case %) (clojure.string/upper-case map-name)) maps)))

(defn votes-formatted [match-m]
  (let [votes (:votes match-m)
       team1-name (:team1-name (:gen-info match-m))
       team2-name (:team2-name (:gen-info match-m))
       map-s (reduce #(conj % (:map %2)) #{} (:map-results match-m))
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
    #(conj %1 {:vote-id (Integer. (re-find #"[0-9]+" %2))
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
    votes)))
