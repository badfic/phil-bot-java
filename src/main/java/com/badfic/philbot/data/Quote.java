package com.badfic.philbot.data;


import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;


@Table("quote")
@NoArgsConstructor
public class Quote extends BaseQuote {

    public Quote(final long messageId, final long channelId, final String quote, final String image, final long userId, final LocalDateTime created) {
        super(messageId, channelId, quote, image, userId, created);
    }

}
