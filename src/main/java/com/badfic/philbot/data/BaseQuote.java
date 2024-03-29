package com.badfic.philbot.data;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Getter
@Setter
@NoArgsConstructor
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

    public BaseQuote(long messageId, long channelId, String quote, String image, long userId, LocalDateTime created) {
        this.messageId = messageId;
        this.channelId = channelId;
        this.quote = quote;
        this.image = image;
        this.userId = userId;
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseQuote baseQuote = (BaseQuote) o;
        return id == baseQuote.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
