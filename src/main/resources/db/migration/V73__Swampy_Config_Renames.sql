ALTER TABLE swampy_games_config
    RENAME COLUMN robinhood_min_percent TO refund_minimum_percent;
ALTER TABLE swampy_games_config
    RENAME COLUMN robinhood_max_percent TO refund_maximum_percent;
ALTER TABLE swampy_games_config
    RENAME COLUMN percent_chance_robinhood_not_happen TO percent_chance_refund_does_not_happen;
ALTER TABLE swampy_games_config
    RENAME COLUMN robinhood_person TO refund_person;
ALTER TABLE swampy_games_config
    RENAME COLUMN robinhood_stopper_person TO no_refund_person;
ALTER TABLE swampy_games_config
    RENAME COLUMN robinhood_stopper_phrase TO no_refund_phrase;
ALTER TABLE swampy_games_config
    RENAME COLUMN robinhood_img TO refund_image;
ALTER TABLE swampy_games_config
    RENAME COLUMN robinhood_stopped_img TO no_refund_image;
ALTER TABLE swampy_games_config
    RENAME COLUMN taxes_min_percent TO taxes_minimum_percent;
ALTER TABLE swampy_games_config
    RENAME COLUMN taxes_max_percent TO taxes_maximum_percent;
ALTER TABLE swampy_games_config
    RENAME COLUMN percent_chance_taxes_not_happen TO percent_chance_taxes_does_not_happen;
ALTER TABLE swampy_games_config
    RENAME COLUMN taxes_stopper_person TO no_taxes_person;
ALTER TABLE swampy_games_config
    RENAME COLUMN taxes_stopper_phrase TO no_taxes_phrase;
ALTER TABLE swampy_games_config
    RENAME COLUMN taxes_img TO taxes_image;
ALTER TABLE swampy_games_config
    RENAME COLUMN taxes_stopped_img TO no_taxes_image;
ALTER TABLE swampy_games_config
    RENAME COLUMN trick_or_treat_points TO this_or_that_points;
ALTER TABLE swampy_games_config
    RENAME COLUMN trick_or_treat_img TO this_or_that_image;
ALTER TABLE swampy_games_config
    RENAME COLUMN trick_or_treat_name TO this_or_that_name;
ALTER TABLE swampy_games_config
    RENAME COLUMN trick_or_treat_treat_emoji TO this_or_that_give_emoji;
ALTER TABLE swampy_games_config
    RENAME COLUMN trick_or_treat_trick_emoji TO this_or_that_take_emoji;
ALTER TABLE swampy_games_config
    RENAME COLUMN member_count_channel TO member_count_voice_channel_id;