ALTER TABLE swampy_games_config
    ADD nsfw_quote_trivia_event_points BIGINT DEFAULT 100,
    ADD nsfw_quote_trivia_msg_id VARCHAR(255),
    ADD nsfw_quote_trivia_correct_answer SMALLINT,
    ADD nsfw_quote_trivia_expiration TIMESTAMP DEFAULT NOW();
