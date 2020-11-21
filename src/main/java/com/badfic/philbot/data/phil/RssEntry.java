package com.badfic.philbot.data.phil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
