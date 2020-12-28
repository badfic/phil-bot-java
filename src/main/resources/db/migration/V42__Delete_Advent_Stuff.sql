ALTER TABLE discord_user
    DROP COLUMN IF EXISTS advent_counter,
    DROP COLUMN IF EXISTS last_advent;

ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS giveaway_msg_id,
    DROP COLUMN IF EXISTS past_giveaway_winners;

DROP TABLE IF EXISTS fire_drill_permission;
