package com.badfic.philbot.data.phil;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nsfw_quote")
@NoArgsConstructor
public class NsfwQuote extends BaseQuote {

    public NsfwQuote(long messageId, long channelId, String quote, String image, long userId, LocalDateTime created) {
        super(messageId, channelId, quote, image, userId, created);
    }

}
