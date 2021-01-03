ALTER TABLE swampy_games_config
    ADD swiper_expiration TIMESTAMP DEFAULT NOW(),
    ADD trivia_expiration TIMESTAMP DEFAULT NOW();
