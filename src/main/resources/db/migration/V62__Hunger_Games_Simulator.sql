CREATE TABLE hg_pronoun (
    id BIGSERIAL PRIMARY KEY,
    subject TEXT,
    object TEXT,
    possessive TEXT,
    self TEXT
);

CREATE TABLE hg_player (
    id BIGSERIAL PRIMARY KEY,
    hp INTEGER,
    pronoun_id BIGSERIAL,
    discord_user_id BIGSERIAL UNIQUE,
    name TEXT UNIQUE,
    CONSTRAINT fk_hg_player_discord_id FOREIGN KEY (discord_user_id) REFERENCES discord_user(id),
    CONSTRAINT fk_hg_player_pronoun_id FOREIGN KEY (pronoun_id) REFERENCES hg_pronoun(id)
);

CREATE TABLE hg_round (
    id BIGSERIAL PRIMARY KEY,
    name TEXT,
    description TEXT,
    opening_round BOOLEAN DEFAULT FALSE
);

CREATE UNIQUE INDEX ON hg_round(opening_round) WHERE opening_round = true;

CREATE TABLE hg_outcome (
    id BIGSERIAL PRIMARY KEY,
    outcome_text TEXT,
    num_players INTEGER,
    player_1_hp INTEGER,
    player_2_hp INTEGER,
    player_3_hp INTEGER,
    player_4_hp INTEGER
);

CREATE TABLE hg_game (
    id SMALLINT PRIMARY KEY,
    name TEXT,
    round_counter INTEGER
);
