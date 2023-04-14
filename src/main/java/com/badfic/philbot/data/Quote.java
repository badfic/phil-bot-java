package com.badfic.philbot.data;


import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;


@Table("quote")
@NoArgsConstructor
public class Quote extends BaseQuote {

    public Quote(long messageId, long channelId, String quote, String image, long userId, LocalDateTime created) {
        super(messageId, channelId, quote, image, userId, created);
    }

}
