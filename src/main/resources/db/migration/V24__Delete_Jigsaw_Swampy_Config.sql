ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS jigsaw_awaiting,
    DROP COLUMN IF EXISTS jigsaw_message_id,
    DROP COLUMN IF EXISTS past_victims,
    ADD most_recent_taxes BIGINT DEFAULT 0;
