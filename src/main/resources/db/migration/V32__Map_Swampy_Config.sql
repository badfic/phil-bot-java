ALTER TABLE swampy_games_config
    ADD map_phrase VARCHAR(255),
    ADD map_trivia_expiration TIMESTAMP;

ALTER TABLE discord_user
    ADD accepted_map_trivia TIMESTAMP;
