ALTER TABLE discord_user
    ADD last_steal TIMESTAMP DEFAULT NOW();
