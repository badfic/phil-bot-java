package com.badfic.philbot.data.phil;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.util.Objects;

@MappedSuperclass
public abstract class BaseQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public BaseQuote() {
    }

    public BaseQuote(long messageId, long channelId, String quote, String image, long userId, LocalDateTime created) {
        this.messageId = messageId;
        this.channelId = channelId;
        this.quote = quote;
        this.image = image;
        this.userId = userId;
        this.created = created;
    }

    public long getId() {
        return id;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
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
