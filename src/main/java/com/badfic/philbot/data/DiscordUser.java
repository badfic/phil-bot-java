package com.badfic.philbot.data;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "discord_user")
public class DiscordUser {

    @Id
    private String id;

    @OneToMany(mappedBy = "phrase", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Phrase> phrases = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<Phrase> getPhrases() {
        return phrases;
    }

    public void setPhrases(Set<Phrase> phrases) {
        this.phrases = phrases;
    }

}
