CREATE TABLE john_responses_config (
    id SMALLINT PRIMARY KEY,
    nsfw_config JSON NOT NULL,
    sfw_config JSON NOT NULL
);
