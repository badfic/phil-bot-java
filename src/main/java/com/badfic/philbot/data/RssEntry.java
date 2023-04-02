package com.badfic.philbot.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rss_entry")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "link")
public class RssEntry {
    @Id
    private String link;

    @Column
    private String feedUrl;

    public RssEntry(String link, String feedUrl) {
        this.link = link;
        this.feedUrl = feedUrl;
    }

}
