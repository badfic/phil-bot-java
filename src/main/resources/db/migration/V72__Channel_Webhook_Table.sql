CREATE TABLE channel_webhook (
    channel_id BIGINT PRIMARY KEY,
    webhook_id BIGINT NOT NULL,
    token TEXT NOT NULL
);

ALTER TABLE swampy_games_config
    ADD antonia_nickname TEXT DEFAULT E'Antonia',
    ADD antonia_avatar TEXT DEFAULT E'https://cdn.discordapp.com/attachments/794506942906761226/1096712794457526342/antonia-april-2023.png',
    ADD behrad_nickname TEXT DEFAULT E'Behrad',
    ADD behrad_avatar TEXT DEFAULT E'https://cdn.discordapp.com/attachments/794506942906761226/1096715373245632558/behrad-april-2023.png',
    ADD john_nickname TEXT DEFAULT E'John',
    ADD john_avatar TEXT DEFAULT E'https://cdn.discordapp.com/attachments/794506942906761226/1096715500895076412/john-april-2023.png',
    ADD keanu_nickname TEXT DEFAULT E'Keanu',
    ADD keanu_avatar TEXT DEFAULT E'https://cdn.discordapp.com/attachments/794506942906761226/1096715831330754560/keanu-april-2023.png';