ALTER TABLE swampy_games_config
    ADD giveaway_msg_id bigint DEFAULT 0,
    ADD past_giveaway_winners JSON;
