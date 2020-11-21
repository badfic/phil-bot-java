CREATE TABLE rss_entry (
    link TEXT PRIMARY KEY,
    feed_url TEXT
);

CREATE INDEX rss_entry__feed_url__idx ON rss_entry (feed_url);
