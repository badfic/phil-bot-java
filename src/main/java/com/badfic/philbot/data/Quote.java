package com.badfic.philbot.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quote")
@NoArgsConstructor
public class Quote extends BaseQuote {

    public Quote(long messageId, long channelId, String quote, String image, long userId, LocalDateTime created) {
        super(messageId, channelId, quote, image, userId, created);
    }

}
