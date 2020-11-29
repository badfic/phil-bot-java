ALTER TABLE discord_user
    ADD advent_counter INTEGER DEFAULT 0,
    ADD last_advent TIMESTAMP DEFAULT NOW();
