ALTER TABLE discord_user
    ADD last_flip TIMESTAMP DEFAULT NOW();
