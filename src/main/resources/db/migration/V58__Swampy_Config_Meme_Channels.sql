ALTER TABLE swampy_games_config
    ADD sfw_saved_memes_channel_id BIGINT DEFAULT 0,
    ADD nsfw_saved_memes_channel_id BIGINT DEFAULT 0;
