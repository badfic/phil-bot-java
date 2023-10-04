ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS swiper_awaiting;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS swiper_savior;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS no_swiping_phrase;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS swiper_expiration;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS swiper_points;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS carrot_event_points;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS carrot_img;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS carrot_name;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS potato_event_points;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS potato_img;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS potato_name;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS glitter_event_points;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS glitter_img;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS glitter_name;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS stonks_max_points;
