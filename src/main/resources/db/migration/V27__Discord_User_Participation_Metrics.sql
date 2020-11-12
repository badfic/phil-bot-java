ALTER TABLE discord_user
    ADD boost_participations BIGINT DEFAULT 0,
    ADD swiper_participations BIGINT DEFAULT 0;