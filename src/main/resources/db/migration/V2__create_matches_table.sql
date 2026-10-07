CREATE TABLE matches (
                         id BIGINT PRIMARY KEY,
                         utc_date TIMESTAMP WITH TIME ZONE NOT NULL,
                         status VARCHAR(50) NOT NULL,
                         home_team_id BIGINT NOT NULL REFERENCES teams(id),
                         away_team_id BIGINT NOT NULL REFERENCES teams(id),
                         home_goals INT,
                         away_goals INT
);