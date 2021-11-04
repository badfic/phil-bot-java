package com.badfic.philbot.data.phil;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "nsfw_quote")
public class NsfwQuote extends BaseQuote {

    public NsfwQuote() {
        super();
    }

    public NsfwQuote(long messageId, long channelId, String quote, String image, long userId, LocalDateTime created) {
        super(messageId, channelId, quote, image, userId, created);
    }

}
