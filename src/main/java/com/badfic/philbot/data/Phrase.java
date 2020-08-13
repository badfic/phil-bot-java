package com.badfic.philbot.data;

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "phrase")
public class Phrase {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String phrase;

    @Column
    private long counter;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "discord_user_id", nullable = false)
    private DiscordUser discordUser;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public DiscordUser getDiscordUser() {
        return discordUser;
    }

    public void setDiscordUser(DiscordUser discordUser) {
        this.discordUser = discordUser;
    }

    @Override
    public String toString() {
        return "{" +
                "phrase='" + phrase + '\'' +
                ", count=" + counter +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phrase phrase = (Phrase) o;
        return id.equals(phrase.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
