UPDATE discord_user SET update_time = NOW() WHERE update_time IS NULL;
ALTER TABLE discord_user
    ALTER COLUMN update_time SET DEFAULT NOW(),
    ALTER COLUMN update_time SET NOT NULL;

UPDATE discord_user SET last_slots = NOW() WHERE last_slots IS NULL;
ALTER TABLE discord_user
    ALTER COLUMN last_slots SET DEFAULT NOW(),
    ALTER COLUMN last_slots SET NOT NULL;

UPDATE discord_user SET last_message_bonus = NOW() WHERE last_message_bonus IS NULL;
ALTER TABLE discord_user
    ALTER COLUMN last_message_bonus SET DEFAULT NOW(),
    ALTER COLUMN last_message_bonus SET NOT NULL;

UPDATE discord_user SET last_vote = NOW() WHERE last_vote IS NULL;
ALTER TABLE discord_user
    ALTER COLUMN last_vote SET DEFAULT NOW(),
    ALTER COLUMN last_vote SET NOT NULL;

UPDATE discord_user SET accepted_boost = NOW() WHERE accepted_boost IS NULL;
ALTER TABLE discord_user
    ALTER COLUMN accepted_boost SET DEFAULT NOW(),
    ALTER COLUMN accepted_boost SET NOT NULL;

UPDATE discord_user SET accepted_map_trivia = NOW() WHERE accepted_map_trivia IS NULL;
ALTER TABLE discord_user
    ALTER COLUMN accepted_map_trivia SET DEFAULT NOW(),
    ALTER COLUMN accepted_map_trivia SET NOT NULL;
