CREATE TABLE quote (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT UNIQUE,
    channel_id BIGINT,
    quote TEXT,
    user_id BIGINT,
    created TIMESTAMP DEFAULT NOW()
);
CREATE UNIQUE INDEX quote__msg_id__idx ON quote(message_id);
