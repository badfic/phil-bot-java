ALTER TABLE discord_user
    ADD quote_trivia_points BIGINT DEFAULT 0;

ALTER TABLE swampy_games_config
    ADD quote_trivia_event_points BIGINT DEFAULT 100,
    ADD quote_trivia_msg_id VARCHAR(255),
    ADD quote_trivia_correct_answer SMALLINT,
    ADD quote_trivia_expiration TIMESTAMP DEFAULT NOW();
