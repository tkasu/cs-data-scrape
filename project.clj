(defproject cs-data-scrape "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
 :dependencies [[org.clojure/clojure "1.8.0"]
                [org.clojure/java.jdbc "0.7.0-alpha1"]
                [postgresql "9.3-1102.jdbc41"]
                [clojure-csv/clojure-csv "2.0.1"]
                [cheshire "5.6.3"]
                [enlive "1.1.5"]
                [clj-http "2.2.0"]
                [clj-webdriver "0.7.2"]
                [org.seleniumhq.selenium/selenium-java "2.53.1"]
                [clj-time "0.12.0"]]
  :main ^:skip-aot cs-data-scrape.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
