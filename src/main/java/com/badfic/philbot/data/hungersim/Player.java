package com.badfic.philbot.data.hungersim;

import com.badfic.philbot.data.DiscordUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
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

    public Player(final DiscordUser discordUser, final Pronoun pronoun) {
        this.discordUser = discordUser.getId();
        this.pronoun = pronoun.getId();
    }

    public Player(final String name, final Pronoun pronoun) {
        this.name = name;
        this.pronoun = pronoun.getId();
    }

    public void setEffectiveNameViaJda(final JDA jda) {
        if (discordUser != null) {
            final var memberById = jda.getGuilds().getFirst().getMemberById(discordUser);
            if (memberById != null) {
                effectiveName = memberById.getEffectiveName();
                return;
            }

            final var userById = jda.getUserById(discordUser);
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
