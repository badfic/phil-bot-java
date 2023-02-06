ALTER TABLE discord_user
    DROP COLUMN IF EXISTS advent_counter,
    DROP COLUMN IF EXISTS last_advent,
    DROP COLUMN IF EXISTS advent_points;
