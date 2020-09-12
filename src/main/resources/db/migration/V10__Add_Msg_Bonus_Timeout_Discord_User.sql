ALTER TABLE discord_user
    ADD last_message_bonus TIMESTAMP DEFAULT NOW();
