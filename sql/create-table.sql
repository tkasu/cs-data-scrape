CREATE TABLE match
(
match_id VARCHAR(255) PRIMARY KEY,
link VARCHAR(255) UNIQUE NOT NULL,
match_date DATE
);

/*
{:team1-name "Emc", :team1-score 0, :team1-result "L", :team2-name "3 NEW 2 OLD", :team2-score 2, :team2-result "W"}
*/
CREATE TABLE match_result
(
id SERIAL PRIMARY KEY,
match_id VARCHAR(255),
team1_name VARCHAR(255) NOT NULL,
team2_name VARCHAR(255) NOT NULL,
team1_score INT NOT NULL,
team2_score INT NOT NULL,
team1_result VARCHAR(1) NOT NULL,
team2_result VARCHAR(1) NOT NULL,
FOREIGN KEY (match_id) REFERENCES match(match_id)
);

/*
:map-results [{:map "cobblestone", :map-num 1, :map-results {:team1-score-map 10, :team2-side-r1 "T", :team1-score-r2 7, :team1-score-r1 3, :team2-score-r1 12, :team2-score-r2 4, :team2-side-r2 "CT", :team2-score-map 16, :team1-side-r1 "CT", :team1-side-r2 "T"}} {:map "cache", :map-num 2, :map-results {:team1-score-map 5, :team2-side-r1 "CT", :team1-score-r2 1, :team1-score-r1 4, :team2-score-r1 11, :team2-score-r2 5, :team2-side-r2 "T", :team2-score-map 16, :team1-side-r1 "T", :team1-side-r2 "CT"}} {:map "dust2", :map-num 3, :map-results nil}],
*/

CREATE TABLE match_map_result
(
result_id INT,
map_num INT NOT NULL,
map_name VARCHAR(255) NOT NULL,
team1_score_map INT NOT NULL,
team2_score_map INT NOT NULL,
PRIMARY KEY (result_id, map_num),
FOREIGN KEY (result_id) REFERENCES match_result(id)
);
