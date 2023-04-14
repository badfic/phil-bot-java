package com.badfic.philbot.data;


import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;


@Table("nsfw_quote")
@NoArgsConstructor
public class NsfwQuote extends BaseQuote {

    public NsfwQuote(long messageId, long channelId, String quote, String image, long userId, LocalDateTime created) {
        super(messageId, channelId, quote, image, userId, created);
    }

}
