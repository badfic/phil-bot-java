package com.badfic.philbot.data.phil;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "quote")
public class Quote extends BaseQuote {

    public Quote() {
        super();
    }

    public Quote(long messageId, long channelId, String quote, String image, long userId, LocalDateTime created) {
        super(messageId, channelId, quote, image, userId, created);
    }

}
