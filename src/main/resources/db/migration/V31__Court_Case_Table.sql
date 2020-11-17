CREATE TABLE court_case (
    defendant_id BIGSERIAL PRIMARY KEY,
    accuser_id BIGINT,
    trial_message_id BIGINT,
    crime TEXT,
    trial_date TIMESTAMP,
    release_date TIMESTAMP
);
