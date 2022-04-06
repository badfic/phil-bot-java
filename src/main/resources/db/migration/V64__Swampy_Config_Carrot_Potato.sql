ALTER TABLE swampy_games_config
    ADD carrot_event_points INTEGER DEFAULT 1000,
    ADD potato_event_points INTEGER DEFAULT 1000,
    ADD carrot_img TEXT DEFAULT E'https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png',
    ADD potato_img TEXT DEFAULT E'https://cdn.discordapp.com/attachments/752665380182425677/782811729599528960/swampy_monday_every_image_AHHHH.png';