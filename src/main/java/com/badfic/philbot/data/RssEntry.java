package com.badfic.philbot.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("rss_entry")
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
