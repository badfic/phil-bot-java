ALTER TABLE discord_user
    ADD trivia_guid_time TIMESTAMP DEFAULT NOW(),
    ADD trivia_guid UUID;

ALTER TABLE swampy_games_config
    ADD trivia_guid UUID,
    ADD trivia_msg_id VARCHAR(255);

CREATE TABLE trivia (
    id UUID PRIMARY KEY,
    question TEXT,
    answer_a VARCHAR(255),
    answer_b VARCHAR(255),
    answer_c VARCHAR(255),
    correct_answer SMALLINT
);