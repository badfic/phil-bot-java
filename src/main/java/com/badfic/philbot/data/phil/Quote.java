package com.badfic.philbot.data.phil;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

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
