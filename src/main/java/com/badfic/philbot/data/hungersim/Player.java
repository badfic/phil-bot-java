package com.badfic.philbot.data.hungersim;

import com.badfic.philbot.data.DiscordUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("hg_player")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Player {

    @Id
    private Long id;

    @Column
    private Integer hp = 10;

    @Column("pronoun_id")
    private Long pronoun;

    @Column("discord_user_id")
    private String discordUser;

    @Column
    private String name;

    @Column("game_id")
    private Short game;

    @Transient
    private String effectiveName;

    public Player(DiscordUser discordUser, Pronoun pronoun) {
        this.discordUser = discordUser.getId();
        this.pronoun = pronoun.getId();
    }

    public Player(String name, Pronoun pronoun) {
        this.name = name;
        this.pronoun = pronoun.getId();
    }

    public void setEffectiveNameViaJda(JDA jda) {
        if (discordUser != null) {
            Member memberById = jda.getGuilds().get(0).getMemberById(discordUser);
            if (memberById != null) {
                effectiveName = memberById.getEffectiveName();
                return;
            }

            User userById = jda.getUserById(discordUser);
            if (userById != null) {
                effectiveName = userById.getName();
                return;
            }

            effectiveName = "<@" + discordUser + ">";
            return;
        }

        effectiveName = name;
    }

}
