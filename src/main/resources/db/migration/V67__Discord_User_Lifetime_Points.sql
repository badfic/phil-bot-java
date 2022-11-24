ALTER TABLE discord_user
    ADD lifetime_upvotes_received BIGINT DEFAULT 0,
    ADD lifetime_upvotes_given BIGINT DEFAULT 0,
    ADD lifetime_slots_wins BIGINT DEFAULT 0,
    ADD lifetime_slots_losses BIGINT DEFAULT 0,
    ADD lifetime_maps_correct BIGINT DEFAULT 0,
    ADD lifetime_trivias_correct BIGINT DEFAULT 0,
    ADD lifetime_quote_trivias_correct BIGINT DEFAULT 0,
    ADD lifetime_sweepstakes_wins BIGINT DEFAULT 0,
    ADD lifetime_no_no_words BIGINT DEFAULT 0,
    ADD lifetime_messages BIGINT DEFAULT 0,
    ADD lifetime_pictures BIGINT DEFAULT 0,
    ADD lifetime_boost_participations BIGINT DEFAULT 0,
    ADD lifetime_voice_chat_minutes BIGINT DEFAULT 0;
