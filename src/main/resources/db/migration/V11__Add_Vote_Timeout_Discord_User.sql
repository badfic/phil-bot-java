ALTER TABLE discord_user
    ADD last_vote TIMESTAMP DEFAULT NOW();
