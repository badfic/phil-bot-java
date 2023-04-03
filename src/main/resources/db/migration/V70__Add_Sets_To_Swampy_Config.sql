ALTER TABLE swampy_games_config
    ADD slots_emoji JSON DEFAULT '["🍓","🍍","🍊","🍋","🍇","🍉","🍌","🍒","🍎"]'::JSON,
    ADD boost_words JSON DEFAULT '["boost"]'::JSON,
    ADD monthly_colors JSON DEFAULT '["#599111"]'::JSON,
    ADD embed_footers JSON DEFAULT '["powered by 777"]'::JSON;
