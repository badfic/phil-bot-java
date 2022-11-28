package com.badfic.philbot.data.phil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rss_entry")
public class RssEntry {
    @Id
    private String link;

    @Column
    private String feedUrl;

    public RssEntry(String link, String feedUrl) {
        this.link = link;
        this.feedUrl = feedUrl;
    }

    public RssEntry() {
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }
}
