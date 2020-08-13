CREATE TABLE discord_user (
    id VARCHAR(255) PRIMARY KEY
);

CREATE TABLE phrase (
    id UUID PRIMARY KEY,
    phrase VARCHAR(255) NOT NULL,
    counter BIGINT NOT NULL DEFAULT 0,
    discord_user_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (discord_user_id) REFERENCES discord_user(id)
);
