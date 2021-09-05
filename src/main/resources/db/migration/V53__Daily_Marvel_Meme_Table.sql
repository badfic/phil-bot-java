CREATE TABLE daily_marvel_meme (
    message_id BIGINT PRIMARY KEY,
    message TEXT,
    image_url TEXT,
    time_created TIMESTAMP,
    time_edited TIMESTAMP
);
