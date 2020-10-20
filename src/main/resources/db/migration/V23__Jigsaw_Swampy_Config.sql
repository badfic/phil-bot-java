ALTER TABLE swampy_games_config
    ADD jigsaw_awaiting VARCHAR(255),
    ADD jigsaw_message_id VARCHAR(255),
    ADD past_victims JSON;
