package com.badfic.philbot.data;

import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public abstract class BaseQuote {

    @Id
    private long id;

    @Column
    private long messageId;

    @Column
    private long channelId;

    @Column
    private String quote;

    @Column
    private String image;

    @Column
    private long userId;

    @Column
    private LocalDateTime created;

    public BaseQuote(final long messageId, final long channelId, final String quote, final String image, final long userId, final LocalDateTime created) {
        this.messageId = messageId;
        this.channelId = channelId;
        this.quote = quote;
        this.image = image;
        this.userId = userId;
        this.created = created;
    }

}
