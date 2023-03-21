package com.badfic.philbot.data.hungersim;

import com.badfic.philbot.data.DiscordUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

@Entity
@Table(name = "hg_player")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
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

    public Player(DiscordUser discordUser, Pronoun pronoun) {
        this.discordUser = discordUser;
        this.pronoun = pronoun;
    }

    public Player(String name, Pronoun pronoun) {
        this.name = name;
        this.pronoun = pronoun;
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

}
