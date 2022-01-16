package com.badfic.philbot.data.hungersim;

import com.badfic.philbot.data.DiscordUser;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

@Entity
@Table(name = "hg_player")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Integer hp = 10;

    @OneToOne
    private Pronoun pronoun;

    @OneToOne
    private DiscordUser discordUser;

    @Column
    private String name;

    @Transient
    private String effectiveName;

    public Player() {
    }

    public Player(DiscordUser discordUser, Pronoun pronoun) {
        this.discordUser = discordUser;
        this.pronoun = pronoun;
    }

    public Player(String name, Pronoun pronoun) {
        this.name = name;
        this.pronoun = pronoun;
    }

    public Long getId() {
        return id;
    }

    public Integer getHp() {
        return hp;
    }

    public void setHp(Integer hp) {
        this.hp = hp;
    }

    public Pronoun getPronoun() {
        return pronoun;
    }

    public void setPronoun(Pronoun pronoun) {
        this.pronoun = pronoun;
    }

    public DiscordUser getDiscordUser() {
        return discordUser;
    }

    public void setDiscordUser(DiscordUser discordUser) {
        this.discordUser = discordUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEffectiveName() {
        return effectiveName;
    }

    public void setEffectiveNameViaJda(JDA jda) {
        if (discordUser != null) {
            Member memberById = jda.getGuilds().get(0).getMemberById(discordUser.getId());
            if (memberById != null) {
                effectiveName = memberById.getEffectiveName();
                return;
            }

            User userById = jda.getUserById(discordUser.getId());
            if (userById != null) {
                effectiveName = userById.getName();
                return;
            }

            effectiveName = "<@!" + discordUser.getId() + ">";
            return;
        }

        effectiveName = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Player player = (Player) o;
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
