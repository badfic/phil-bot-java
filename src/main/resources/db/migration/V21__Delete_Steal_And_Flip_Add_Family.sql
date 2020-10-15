ALTER TABLE discord_user
    DROP COLUMN IF EXISTS last_steal,
    DROP COLUMN IF EXISTS last_flip,
    ADD family JSON;
