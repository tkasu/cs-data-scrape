(ns cs-data-scrape.scrape
  (:require [clj-webdriver.taxi :as t]
            [clj-webdriver.firefox :as t-ff]
            [net.cgrand.enlive-html :as e]
            [clojure.string :as str]
            [clj-time.core :as time]
            [clj-time.format :as timef]
            [clj-http.client :as http]
            [cheshire.core :as json]))

(def url-base "http://www.hltv.org")

(def url "http://www.hltv.org/matches/archive/")

(def test-match-url "http://www.hltv.org/match/2303771-kalashnikov-47-archangels-esea-premier-season-22-europe")

(def formatter-match-date (timef/formatter "MMM dd, YYYY"))

(defn col-index [col-name head-map]
  (:column-num
   (first
    (filter #(= (:content %) col-name) head-map))))

(defn remove-from-col-that-start-with [col-beg col]
  "Removes entities from collections that begin with col-beg"
  (remove (fn [c] (re-find (re-pattern (str "^" col-beg)) (str c))) col))

(defn column-raw-data []
      (first
       (:content
        (first
         (e/html-snippet
          (t/html (t/find-element {:css "div#matches>table>tbody"})))))))

(defn clean-single-match-data [raw-single-match header-data]
  (:content
   (nth
    (remove-from-col-that-start-with
     "\n "
     (:content
      ;TODO scrape nth instead of first available match
      raw-single-match))
    (col-index :Date header-data))))

(defn match-raw-data []
  (remove-from-col-that-start-with
   "\n "
   (rest
    (:content
     (first
      (e/html-snippet
       (t/html (t/find-element {:css "div#matches>table>tbody"}))))))))

(defn cleaned-thead-data [col-raw]
  "Cleanup for raw column data."
  (filter
   #(= (:tag %) :td)
   (remove-from-col-that-start-with "\n  " (:content col-raw))))

(defn thead-cols [thead-data]
  "Gets hltv.org header data and creates a seq of keyword from it."
  (reduce (fn [cum next]
            (let [thead-keyword (keyword (first (:content next)))]
              (conj cum {:content thead-keyword})))
          []
          thead-data))

(defn thead-data-w-index [thead-col-data]
  "Adds column number to thead-data. Should be called to collection after function thead-cols."
  (reduce
   #(conj %1 (assoc %2 :column-num (.indexOf thead-col-data %2)))
   []
   thead-col-data))

(defn filter-newer-than [seq date-field-fn iso-date]
  (let [iso-formatter (timef/formatter "YYYY-MM-dd")]
    (filter #(time/after? (date-field-fn %) (timef/parse iso-formatter iso-date)) seq)))

 (defn match-general-data []
   (let [teams (->
                {:xpath "//span[starts-with(@class,'matchScore')]/.."}
                t/find-elements )
         team1-raw (t/text (first teams))
         team2-raw (t/text (first (rest teams)))
         team1-name (as-> team1-raw v
                          (str/split v #"\s")
                          (drop-last v)
                          (str/join " " v))
         team2-name (as-> team2-raw v
                          (str/split v #"\s")
                          (rest v)
                          (str/join " " v))
         team1-score (-> team1-raw
                         (str/split #"\s")
                         last
                         Integer.)
         team2-score (-> team2-raw
                         (str/split #"\s")
                         first
                         Integer.)]
     {:team1-name team1-name
      :team1-score team1-score
      :team1-result (cond
                     (> team1-score team2-score) "W"
                     (< team1-score team2-score) "L"
                     (= team1-score team2-score) "D"
                     :else :error)
      :team2-name team2-name
      :team2-score team2-score
      :team2-result (cond
                     (> team2-score team1-score) "W"
                     (< team2-score team1-score) "L"
                     (= team2-score team1-score) "D"
                     :else :error)}))

(defn match-votes []
   (let [raw-html
         (when (t/exists? {:xpath "//div[text()='Veto process']/.."})
          (->>
                                        ;xpath to parent div-element of 'Veto process textbox'
           {:xpath "//div[text()='Veto process']/.."}
           t/find-element
           t/html
           e/html-snippet
           first
           :content
           (remove-from-col-that-start-with "\n  ")))
         vote-html (rest raw-html)]
     (->>
      (reduce #(conj %1 (first (:content %2)))  #{} vote-html)
      (remove nil?))))

(defn team-by-css-style [css-style]
  (cond
   (= css-style "color: red;") "T"
   (= css-style "color: blue;") "CT"
   :else :error))

   (def xf-parse-map-res 
     (fn [xf]
       (let [idx (volatile! 0)]
         (fn
           ([] (xf))
           ([result] (xf result))
           ([result item]
            (let [index idx]  
              (do (vreset! index (inc @index))
                  (xf result
                      {(keyword (str "team1-score-r" @index)) 
                       (-> item
                           (nth 0)
                           (t/attribute :text)
                           Integer.)
                       (keyword (str "team2-score-r" @index))
                       (-> item
                           (nth 1)
                           (t/attribute :text)
                           Integer.)
                       (keyword (str "team1-side-r" @index)) 
                       (-> item
                           (nth 0)
                           (t/attribute :style)
                           team-by-css-style)
                       (keyword (str "team2-side-r" @index)) 
                       (-> item
                           (nth 1)
                           (t/attribute :style)
                           team-by-css-style)}))))))))

  (defn parse-map-result [span-parent-div-elem]
    (let [res-span (-> span-parent-div-elem
                       t/xpath
                       (str "/span"))]
      (when (t/exists? {:xpath res-span})
        (let [span-elems (t/find-elements {:xpath res-span})
              span-elems-for-round (partition-all 2 span-elems)
              score-map {:team1-score-map (-> (first span-elems-for-round)
                                              (nth 0)
                                              (t/attribute :text)
                                              Integer.)
                         :team2-score-map (-> (first span-elems-for-round)
                                              (nth 1)
                                              (t/attribute :text)
                                              Integer.)}]
              (transduce xf-parse-map-res conj score-map (rest span-elems-for-round))))))

(defn get-child-img-src [elem]
    (let [elem-xpath (t/xpath elem)
          child-img-xpath
          (if-not (= \] (last elem-xpath))
            ;t/xpath omit div number for the first div
            (str elem-xpath "[1]/img")
            (str elem-xpath "/img")) ]
      (when (t/exists? {:xpath child-img-xpath})
       (-> (t/find-element {:xpath child-img-xpath})
           (t/attribute :src)))))

(defn get-map-from-link [link]
    "Example link: http://static.hltv.org//images/hotmatch/overpass.png"
    (when link
        (-> (re-find #"/hotmatch/(.+?).png" link)
            (get 1))))

(defn match-results-for-maps [] 
  (let [elements (t/find-elements {:xpath "//div[@class='hotmatchboxheader']/div[starts-with(text(),'Maps')]/../../div[@class='hotmatchbox']/div"})
        next-map (atom nil)
        index (atom 0)
        map-num (atom 0)
        results (atom [])]
    (loop [elems elements]
      (if (empty? elems)
        @results
        (do
          (if @next-map
            (do
              (reset! map-num (inc @map-num))
              (reset! results (conj @results {:map @next-map
                                              :map-num @map-num
                                              :map-results (parse-map-result (first elems))}))))
          (reset! next-map (-> elems
                               first
                               get-child-img-src
                               get-map-from-link))
          (println (str @next-map " " @index))
          (reset! index (inc @index))
          (recur (rest elems)))))))

(defn scrape-matches-after! [iso-date]
  (do
    (t/set-driver! {:browser :firefox})
    (t/to url)
    ;Random wait 0-5s
    (loop [i 0
           result #{}]
      (let [result-filtered (set (filter-newer-than result :match-date iso-date))]
       (if (not (= result result-filtered))
         (do
           (println (str "Page " (+ i 1) " reached. Analysis done"))
           (t/close)
                                        ;Return result set at the end
           result-filtered)
         (do
                                        ;Wait 2-5s before scanning the page. Currently scrape has problems if the scan is done < 0.1s after the page load.
           (println (time (Thread/sleep (+ 2000 (rand 3000)))))
                                        ;Wait until matches-table has at least one match with link to the match-page (this is not working pefrectly, see the comment before wait.)
           (t/wait-until (t/exists? "div#matches>table>tbody>tr>td>a[href]"))
           (let [header-data
                 (thead-data-w-index
                  (thead-cols
                   (cleaned-thead-data
                    (column-raw-data))))
                 match-data (match-raw-data)
                 page-matches
                 (reduce
                  (fn [acc next]
                    (let [cleaned-match
                          (first (clean-single-match-data next header-data))]
                      (conj
                       acc
                       {:id (str/replace (:href (:attrs cleaned-match)) #"/match/"  "")
                        :link (str url-base (:href (:attrs cleaned-match)))
                                        ;Date stored as org.joda.time.DateTime.
                                        ;TBD if this should be converted to java.sql.Date in this phase or during database import
                        :match-date (timef/parse formatter-match-date (first (:content cleaned-match)))})))
                  []
                  match-data)]
             (println (t/find-element {:tag :a
                                       :text (+ i 1)}))
             (println "-----------------------------------------------")
             (t/wait-until (t/exists? {:tag :a
                                       :text (+ i 1)})
                                        ;Timeout 10s
                           10000)
                                        ;Dirty fix to button not clickable exception, scrolls page down
             (loop [j 0]
               (when-not
                   (try
                     (do
                       (t/click (t/find-element {:tag :a :href (str "javascript:nextPage(" (+ (* i 100) 100) ");")}))
                                        ;If click was successful (no exception) return true.
                       true)
                     (catch org.openqa.selenium.WebDriverException e
                       (do
                         (when (>= j 9)
                           (throw e))
                         (println "WebDriverException, scrolling down and trying again.")
                         (t/execute-script (str "window.scrollBy(0," 100 ")")))))
                 (recur (inc j))))
             (recur (inc i) (into result page-matches)))))))))

(defn scrape-match! [url]
  (t/set-driver! {:browser :firefox})
  (t/to url)
  (do
                                        ;Random wait
    (println (time (Thread/sleep (+ 2000 (rand 3000)))))
                                        ;Wait Maps box is found
    (t/wait-until (t/exists? {:xpath "//div[@class='hotmatchboxheader']/div[starts-with(text(),'Maps')]"}))
    (let [votes (match-votes)
          gen-data (match-general-data)
          map-results (match-results-for-maps)]
      (t/close)
      {:id url
       :gen-info gen-data
       :map-results map-results
       :votes votes})))

(comment 
;Test code snippets
  test-match-url
  
  (t/to test-match-url)

  (t/to "http://google.fi")
  
  (def err-match "http://www.hltv.org/match/2303857-t1-academy-cnb-esea-open-season-22-brazil")

  (spit "test-match.txt" (scrape-match! test-match-url))
  
  (println (match-raw-data))
a
  (def  header-data
    (thead-data-w-index
     (thead-cols
      (cleaned-thead-data
       (column-raw-data)))))

  (def match-data (match-raw-data))

  (def page-matches
    (reduce
     (fn [acc next]
       (let [cleaned-match
             (first (clean-single-match-data next header-data))]
         (conj
          acc
          {:id (str/replace (:href (:attrs cleaned-match)) #"/match/"  "")
           :link (str url-base (:href (:attrs cleaned-match)))
                                        ;Date stored as org.joda.time.DateTime.
                                        ;TBD if this should be converted to java.sql.Date in this phase or during database import
           :match-date (timef/parse formatter-match-date (first (:content cleaned-match)))})))
     []
     match-data))

(println page-matches)

(scrape-match! err-match)

(def t-elems (t/find-elements {:xpath "//div[@class='hotmatchboxheader']/div[starts-with(text(),'Maps')]/../../div[@class='hotmatchbox']/div"}))

(parse-map-result (first t-elems))

(first t-elems)

(def spans (t/find-elements {:xpath (str (t/xpath (nth t-elems 4)) "/span")}))

spans

(def d1 (t/new-driver {:browser :firefox}))

(def d2 (t/new-driver {:browser :firefox}))

(t/to d1 "http://www.hltv.org")

(t/to d2 "http://engadget.com")



t-xf

(def t-t-spans (partition-all 2 spans))

(-> (first t-t-spans)
    (nth 0)
    (t/attribute :text)
    Integer.)

(transduce (map inc) + [1 2 3 4])

(transduce xf-parse-map-res conj (rest t-t-spans))

(-> (first (partition-all 2 spans))
    first
    (t/attribute :text))



(get-child-img-src (first t-elems))

(keyword "lol")

(t/xpath (nth t-elems 2))

(if-not (= \] (last (t/xpath (nth t-elems 2))))
  (str (t/xpath ) ""))

(def t-link "http://static.hltv.org//images/hotmatch/overpass.png")

t-link

(re-find #"png" nil)




(get-map-from-link t-link)

(get-child-img-src-t (nth t-elems 3))

(def scrape-results (scrape-matches-after! "2016-11-15"))

(println scrape-results)

(spit "2016-08-12 2016-08-MATCH-LINKS.txt" scrape-results)

#_(def test-urls (reduce #(conj %1 (:link %2)) [] (take 10 scrape-results)))

(def test-urls (reduce #(conj %1 (:link %2)) [] scrape-results))

test-urls

(def test-res (reduce #(conj %1 (scrape-match! %2)) [] test-urls))

(spit "10-MATCHES.txt" test-res)

(def t-proxy (http/get "http://gimmeproxy.com/api/getProxy"))

(json/parse-string (:body t-proxy) true)

(defn get-proxy [] 
  (-> (http/get "http://gimmeproxy.com/api/getProxy?protocol=http")
      :body
      (json/parse-string true)))

(def test (get-proxy))

(let [p (get-proxy)]
  (println (str (:curl p) " " (:port p))))

(:ipPort test)

(:ip (get-proxy))

(:ip test)

(defn new-ff-driver []
  (let [proxy (get-proxy)]
    (println "ip: " (:ip proxy) " port: " (:port proxy))
    (t/new-driver {:browser :firefox
                   :profile (doto 
                                (t-ff/new-profile)
                              (t-ff/set-preferences 
                               {:network.proxy.type 1
                                :network.proxy.http #_"124.88.67.30:83"  (:ip proxy)
                                :network.proxy.http_port #_"83" (Integer. (:port proxy))
                                :network.proxy.ssl #_"124.88.67.30:83"  (:ip proxy)
                                :network.proxy.ssl_port #_"83" (Integer. (:port proxy))
                                })
                              (t-ff/write-to-disk))})))

(def ff-profile
  (doto (t-ff/new-profile)
    (t-ff/set-preferences 
     {:browser.startup.homepage "http://www.whatismyproxy.com/"
      :network.proxy.type 1
      :network.proxy.http #_"124.88.67.30"  (:ip proxy)
      :network.proxy.http_port #_(Integer. "83") (Integer. (:port proxy))
      :network.proxy.ssl #_"124.88.67.30"  (Integer. (:ip proxy))
      :network.proxy.ssl_port #_(Integer. "83") (Integer. (:port proxy))
      })))

(t-ff/write-to-disk ff-profile)

(def ff-loc "/home/tomikasu/programming/cs-data-scrape")

(spit ff-loc (t-ff/json ff-profile))

(def test-ff (new-ff-driver))

(def lol-test
  (t/new-driver {:browser :firefox
                 :profile ff-profile}))

test-ff

(doto
    )
test-ff

(t/to test-ff "http://www.whatismyproxy.com/")

(t-ff/write-to-disk test-ff)

(json/parse-string t-proxy)

(println t-proxy)

(:ip (:body t-proxy))

)






