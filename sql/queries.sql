-- Map statistics per team
WITH map_score_and_win AS (
SELECT
        match_id, 
        match_map_result.map_name, match_result.team_name, opp.team_name AS opp_team_name, 
        CASE
                WHEN match_map_result.team_score_map > opp_map.team_score_map
                     THEN 1
                WHEN match_map_result.team_score_map = opp_map.team_score_map
                     THEN 0.5
                WHEN match_map_result.team_score_map < opp_map.team_score_map
                     THEN 0
        END AS win_dummy,
        match_map_result.team_score_map, opp_map.team_score_map AS opp_team_score_map
FROM
        match_result
        JOIN match_map_result USING (match_id, side_id)
        JOIN match_result opp USING (match_id)
        JOIN match_map_result opp_map USING (match_id, map_num)
WHERE
        1=1
        AND match_result.side_id <> opp.side_id
        AND opp.side_id = opp_map.side_id 
)
SELECT
        team_name, 
        map_name, 
        COUNT(*) AS times_played,
        SUM(win_dummy) / COUNT(win_dummy) AS win_percentage 
FROM
        map_score_and_win
WHERE
        team_name = 'Cloud9'
        --AND map_name = 'dust2'
GROUP BY
      team_name, map_name
ORDER BY
      team_name, map_name
