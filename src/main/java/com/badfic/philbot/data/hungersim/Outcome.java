package com.badfic.philbot.data.hungersim;

import java.util.List;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("hg_outcome")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Outcome {

    @Id
    private Long id;

    @Column
    private String outcomeText;

    @Column
    private Integer numPlayers = 1;

    @Column("player_1_hp")
    private Integer player1Hp = 0;

    @Column("player_2_hp")
    private Integer player2Hp = 0;

    @Column("player_3_hp")
    private Integer player3Hp = 0;

    @Column("player_4_hp")
    private Integer player4Hp = 0;

    public Outcome(String outcomeText, Integer numPlayers, Integer player1Hp, Integer player2Hp, Integer player3Hp, Integer player4Hp) {
        this.outcomeText = outcomeText;
        this.numPlayers = Objects.requireNonNullElse(numPlayers, 1);
        this.player1Hp = Objects.requireNonNullElse(player1Hp, 0);
        this.player2Hp = Objects.requireNonNullElse(player2Hp, 0);
        this.player3Hp = Objects.requireNonNullElse(player3Hp, 0);
        this.player4Hp = Objects.requireNonNullElse(player4Hp, 0);
    }

    public String apply(Player player1, JDA jda, PronounRepository pronounRepository) {
        player1.setHp(minMaxHp(player1.getHp() + player1Hp));

        List<Player> playerList = List.of(player1);
        return getOutcomeResultString(jda, playerList, pronounRepository);
    }

    public String apply(Player player1, Player player2, JDA jda, PronounRepository pronounRepository) {
        player1.setHp(minMaxHp(player1.getHp() + player1Hp));
        player2.setHp(minMaxHp(player2.getHp() + player2Hp));

        List<Player> playerList = List.of(player1, player2);
        return getOutcomeResultString(jda, playerList, pronounRepository);
    }

    public String apply(Player player1, Player player2, Player player3, JDA jda, PronounRepository pronounRepository) {
        player1.setHp(minMaxHp(player1.getHp() + player1Hp));
        player2.setHp(minMaxHp(player2.getHp() + player2Hp));
        player3.setHp(minMaxHp(player3.getHp() + player3Hp));

        List<Player> playerList = List.of(player1, player2, player3);
        return getOutcomeResultString(jda, playerList, pronounRepository);
    }

    public String apply(Player player1, Player player2, Player player3, Player player4, JDA jda, PronounRepository pronounRepository) {
        player1.setHp(minMaxHp(player1.getHp() + player1Hp));
        player2.setHp(minMaxHp(player2.getHp() + player2Hp));
        player3.setHp(minMaxHp(player3.getHp() + player3Hp));
        player4.setHp(minMaxHp(player4.getHp() + player4Hp));

        List<Player> playerList = List.of(player1, player2, player3, player4);
        return getOutcomeResultString(jda, playerList, pronounRepository);
    }

    private int minMaxHp(int hp) {
        if (hp < -10) {
            return -10;
        } else {
            return Math.min(hp, 10);
        }
    }

    private String getOutcomeResultString(JDA jda, List<Player> playerList, PronounRepository pronounRepository) {
        String result = outcomeText;

        for (int i = 0; i < playerList.size(); i++) {
            Player player = playerList.get(i);

            player.setEffectiveNameViaJda(jda);

            result = StringUtils.replace(result, "{player" + (i + 1) + '}', "<b>" + StringEscapeUtils.escapeHtml4(player.getEffectiveName()) + "</b>");
            Long playerPronounId = player.getPronoun();

            Pronoun pronoun = pronounRepository.findById(playerPronounId).orElseThrow();

            result = StringUtils.replace(result, "{player" + (i + 1) + "_subject}", pronoun.getSubject());
            result = StringUtils.replace(result, "{player" + (i + 1) + "_object}", pronoun.getObject());
            result = StringUtils.replace(result, "{player" + (i + 1) + "_possessive}", pronoun.getPossessive());
            result = StringUtils.replace(result, "{player" + (i + 1) + "_self}", pronoun.getSelf());

            if (player.getHp() <= 0) {
                result += " (<b>" + StringEscapeUtils.escapeHtml4(player.getEffectiveName()) + "</b> died)";
            }
        }

        return result;
    }

}
