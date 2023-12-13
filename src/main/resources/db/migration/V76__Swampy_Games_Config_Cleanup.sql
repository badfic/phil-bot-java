ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS most_recent_taxes;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS refund_minimum_percent;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS refund_maximum_percent;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS percent_chance_refund_does_not_happen;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS refund_person;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS no_refund_person;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS no_refund_phrase;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS refund_image;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS no_refund_image;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS scooter_ankle_points;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS scooter_ankle_img;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS shrekoning_min_points;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS shrekoning_max_points;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS shrekoning_img;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS sweepstakes_points;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS sweepstakes_img;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS taxes_minimum_percent;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS taxes_maximum_percent;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS percent_chance_taxes_does_not_happen;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS taxes_person;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS no_taxes_person;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS no_taxes_phrase;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS taxes_image;
ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS no_taxes_image;
