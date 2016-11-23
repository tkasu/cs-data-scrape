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
match_id VARCHAR(255) NOT NULL,
side_id INT NOT NULL,
team_name VARCHAR(255) NOT NULL,
team_score INT NOT NULL,
team_result VARCHAR(1) NOT NULL,
PRIMARY KEY (match_id, side_id),
FOREIGN KEY (match_id) REFERENCES match(match_id)
);

/*
:map-results [{:map "cobblestone", :map-num 1, :map-results {:team1-score-map 10, :team2-side-r1 "T", :team1-score-r2 7, :team1-score-r1 3, :team2-score-r1 12, :team2-score-r2 4, :team2-side-r2 "CT", :team2-score-map 16, :team1-side-r1 "CT", :team1-side-r2 "T"}} {:map "cache", :map-num 2, :map-results {:team1-score-map 5, :team2-side-r1 "CT", :team1-score-r2 1, :team1-score-r1 4, :team2-score-r1 11, :team2-score-r2 5, :team2-side-r2 "T", :team2-score-map 16, :team1-side-r1 "T", :team1-side-r2 "CT"}} {:map "dust2", :map-num 3, :map-results nil}],
*/

CREATE TABLE match_map_result
(
match_id VARCHAR(255) NOT NULL,
side_id INT NOT NULL,
map_num INT NOT NULL,
map_name VARCHAR(255) NOT NULL,
team_score_map INT,
PRIMARY KEY (match_id, side_id, map_num),
FOREIGN KEY (match_id, side_id) REFERENCES match_result(match_id, side_id)
);

CREATE TABLE match_map_half_result
(
match_id VARCHAR(255) NOT NULL,
side_id INT NOT NULL,
map_num INT NOT NULL,
half_id INT NOT NULL,
half_side VARCHAR(2) NOT NULL,
team_score_half INT NOT NULL,
PRIMARY KEY (match_id, side_id, map_num, half_id),
FOREIGN KEY (match_id, side_id, map_num) REFERENCES match_map_result(match_id, side_id, map_num)
);

        
