CREATE TABLE nsfw_quote (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT UNIQUE,
    channel_id BIGINT,
    quote TEXT,
    image TEXT,
    user_id BIGINT,
    created TIMESTAMP DEFAULT NOW()
);
CREATE UNIQUE INDEX nsfw_quote__msg_id__idx ON nsfw_quote(message_id);
