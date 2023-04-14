ALTER TABLE swampy_games_config
    ALTER COLUMN slots_emoji TYPE TEXT,
    ALTER COLUMN boost_words TYPE TEXT,
    ALTER COLUMN monthly_colors TYPE TEXT,
    ALTER COLUMN embed_footers TYPE TEXT;

ALTER TABLE phil_responses_config
    ALTER COLUMN nsfw_config TYPE TEXT,
    ALTER COLUMN sfw_config TYPE TEXT;

ALTER TABLE keanu_responses_config
    ALTER COLUMN nsfw_config TYPE TEXT,
    ALTER COLUMN sfw_config TYPE TEXT;

ALTER TABLE john_responses_config
    ALTER COLUMN nsfw_config TYPE TEXT,
    ALTER COLUMN sfw_config TYPE TEXT;

ALTER TABLE antonia_responses_config
    ALTER COLUMN nsfw_config TYPE TEXT,
    ALTER COLUMN sfw_config TYPE TEXT;

ALTER TABLE behrad_responses_config
    ALTER COLUMN nsfw_config TYPE TEXT,
    ALTER COLUMN sfw_config TYPE TEXT;

ALTER TABLE discord_user
    ALTER COLUMN family TYPE TEXT;

ALTER TABLE hg_game
    ALTER COLUMN current_outcomes TYPE TEXT;

ALTER TABLE hg_player
    ALTER COLUMN game_id TYPE SMALLINT;
