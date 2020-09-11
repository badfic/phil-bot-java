ALTER TABLE discord_user
    ADD last_slots TIMESTAMP DEFAULT NOW();
