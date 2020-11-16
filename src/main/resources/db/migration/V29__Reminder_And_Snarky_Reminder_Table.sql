CREATE TABLE reminder (
    id BIGSERIAL PRIMARY KEY,
    reminder TEXT,
    user_id BIGINT,
    channel_id BIGINT,
    due_date TIMESTAMP DEFAULT NOW()
);

CREATE TABLE snarky_reminder_response (
    id BIGSERIAL PRIMARY KEY,
    response TEXT
);
