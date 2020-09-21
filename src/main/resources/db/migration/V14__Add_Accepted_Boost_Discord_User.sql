ALTER TABLE discord_user
    ADD accepted_boost TIMESTAMP DEFAULT NOW();
