CREATE TABLE hg_pronoun (
    id BIGSERIAL PRIMARY KEY,
    subject TEXT NOT NULL,
    object TEXT NOT NULL,
    possessive TEXT NOT NULL,
    self TEXT NOT NULL
);

CREATE TABLE hg_player (
    id BIGSERIAL PRIMARY KEY,
    hp INTEGER NOT NULL,
    pronoun_id BIGSERIAL NOT NULL,
    discord_user_id VARCHAR(255) UNIQUE,
    name TEXT UNIQUE NOT NULL,
    CONSTRAINT fk_hg_player_discord_id FOREIGN KEY (discord_user_id) REFERENCES discord_user(id),
    CONSTRAINT fk_hg_player_pronoun_id FOREIGN KEY (pronoun_id) REFERENCES hg_pronoun(id)
);

CREATE TABLE hg_round (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL,
    opening_round BOOLEAN DEFAULT FALSE
);

CREATE UNIQUE INDEX ON hg_round(opening_round) WHERE opening_round = true;

CREATE TABLE hg_outcome (
    id BIGSERIAL PRIMARY KEY,
    outcome_text TEXT UNIQUE,
    num_players INTEGER DEFAULT 1,
    player_1_hp INTEGER DEFAULT 0,
    player_2_hp INTEGER DEFAULT 0,
    player_3_hp INTEGER DEFAULT 0,
    player_4_hp INTEGER DEFAULT 0
);

CREATE TABLE hg_round_outcome (
    id BIGSERIAL PRIMARY KEY,
    outcome_id BIGSERIAL NOT NULL,
    round_id BIGSERIAL NOT NULL,
    CONSTRAINT fk_hg_round_outcome_outcome_id FOREIGN KEY (outcome_id) REFERENCES hg_outcome(id),
    CONSTRAINT fk_hg_round_outcome_round_id FOREIGN KEY (round_id) REFERENCES hg_round(id)
);

CREATE INDEX ON hg_round_outcome(round_id);

CREATE TABLE hg_game (
    id SMALLINT PRIMARY KEY,
    name TEXT NOT NULL,
    round_counter INTEGER DEFAULT 0
);
