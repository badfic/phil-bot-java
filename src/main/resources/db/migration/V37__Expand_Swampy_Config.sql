ALTER TABLE swampy_games_config
    DROP COLUMN IF EXISTS story_time_counter,
    ADD normal_msg_points INTEGER DEFAULT 5,
    ADD picture_msg_points INTEGER DEFAULT 250,
    ADD reaction_points INTEGER DEFAULT 7,
    ADD vc_points_per_minute INTEGER DEFAULT 5,
    ADD no_no_words_points INTEGER DEFAULT 100,
    ADD upvote_timeout_minutes INTEGER DEFAULT 1,
    ADD downvote_timeout_minutes INTEGER DEFAULT 1,
    ADD upvote_points_to_upvotee INTEGER DEFAULT 500,
    ADD upvote_points_to_upvoter INTEGER DEFAULT 125,
    ADD downvote_points_from_downvotee INTEGER DEFAULT 100,
    ADD downvote_points_to_downvoter INTEGER DEFAULT 50,
    ADD slots_win_points INTEGER DEFAULT 10000,
    ADD slots_two_of_three_points INTEGER DEFAULT 50,
    ADD picture_msg_timeout_minutes INTEGER DEFAULT 3,
    ADD slots_timeout_minutes INTEGER DEFAULT 3,
    ADD boost_event_points INTEGER DEFAULT 1000,
    ADD map_event_points INTEGER DEFAULT 100,
    ADD trivia_event_points INTEGER DEFAULT 100,
    ADD percent_chance_boost_happens_on_hour INTEGER DEFAULT 15,
    ADD robinhood_min_percent INTEGER DEFAULT 5,
    ADD robinhood_max_percent INTEGER DEFAULT 16,
    ADD percent_chance_robinhood_not_happen INTEGER DEFAULT 30,
    ADD scooter_ankle_points INTEGER DEFAULT 25000,
    ADD shrekoning_min_points INTEGER DEFAULT 200,
    ADD shrekoning_max_points INTEGER DEFAULT 4000,
    ADD stonks_max_points INTEGER DEFAULT 4000,
    ADD sweepstakes_points INTEGER DEFAULT 4000,
    ADD swiper_points INTEGER DEFAULT 1500,
    ADD taxes_min_percent INTEGER DEFAULT 5,
    ADD taxes_max_percent INTEGER DEFAULT 16,
    ADD percent_chance_taxes_not_happen INTEGER DEFAULT 30,
    ADD trick_or_treat_points INTEGER DEFAULT 500;
