package com.badfic.philbot.data.hungersim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "hg_outcome")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Outcome {

    public static final Map<Integer, List<String>> VARIABLES = Map.of(
            1, List.of("{player1}", "{player1_subject}", "{player1_object}", "{player1_possessive}", "{player1_self}"),
            2, List.of("{player2}", "{player2_subject}", "{player2_object}", "{player2_possessive}", "{player2_self}"),
            3, List.of("{player3}", "{player3_subject}", "{player3_object}", "{player3_possessive}", "{player3_self}"),
            4, List.of("{player4}", "{player4_subject}", "{player4_object}", "{player4_possessive}", "{player4_self}"));

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String outcomeText;

    @Column
    private Integer numPlayers = 1;

    @Column(name = "player_1_hp")
    private Integer player1Hp = 0;

    @Column(name = "player_2_hp")
    private Integer player2Hp = 0;

    @Column(name = "player_3_hp")
    private Integer player3Hp = 0;

    @Column(name = "player_4_hp")
    private Integer player4Hp = 0;

    public Outcome(String outcomeText, Integer numPlayers, Integer player1Hp, Integer player2Hp, Integer player3Hp, Integer player4Hp) {
        this.outcomeText = outcomeText;
        this.numPlayers = Objects.requireNonNullElse(numPlayers, 1);
        this.player1Hp = Objects.requireNonNullElse(player1Hp, 0);
        this.player2Hp = Objects.requireNonNullElse(player2Hp, 0);
        this.player3Hp = Objects.requireNonNullElse(player3Hp, 0);
        this.player4Hp = Objects.requireNonNullElse(player4Hp, 0);
    }

    public String apply(Player player1, JDA jda) {
        player1.setHp(minMaxHp(player1.getHp() + player1Hp));

        List<Player> playerList = List.of(player1);
        return getOutcomeResultString(jda, playerList);
    }

    public String apply(Player player1, Player player2, JDA jda) {
        player1.setHp(minMaxHp(player1.getHp() + player1Hp));
        player2.setHp(minMaxHp(player2.getHp() + player2Hp));

        List<Player> playerList = List.of(player1, player2);
        return getOutcomeResultString(jda, playerList);
    }

    public String apply(Player player1, Player player2, Player player3, JDA jda) {
        player1.setHp(minMaxHp(player1.getHp() + player1Hp));
        player2.setHp(minMaxHp(player2.getHp() + player2Hp));
        player3.setHp(minMaxHp(player3.getHp() + player3Hp));

        List<Player> playerList = List.of(player1, player2, player3);
        return getOutcomeResultString(jda, playerList);
    }

    public String apply(Player player1, Player player2, Player player3, Player player4, JDA jda) {
        player1.setHp(minMaxHp(player1.getHp() + player1Hp));
        player2.setHp(minMaxHp(player2.getHp() + player2Hp));
        player3.setHp(minMaxHp(player3.getHp() + player3Hp));
        player4.setHp(minMaxHp(player4.getHp() + player4Hp));

        List<Player> playerList = List.of(player1, player2, player3, player4);
        return getOutcomeResultString(jda, playerList);
    }

    private int minMaxHp(int hp) {
        if (hp < -10) {
            return -10;
        } else {
            return Math.min(hp, 10);
        }
    }

    private String getOutcomeResultString(JDA jda, List<Player> playerList) {
        String result = outcomeText;

        for (int i = 0; i < playerList.size(); i++) {
            Player player = playerList.get(i);

            player.setEffectiveNameViaJda(jda);

            result = StringUtils.replace(result, "{player" + (i + 1) + '}', "<b>" + StringEscapeUtils.escapeHtml4(player.getEffectiveName()) + "</b>");
            result = StringUtils.replace(result, "{player" + (i + 1) + "_subject}", player.getPronoun().getSubject());
            result = StringUtils.replace(result, "{player" + (i + 1) + "_object}", player.getPronoun().getObject());
            result = StringUtils.replace(result, "{player" + (i + 1) + "_possessive}", player.getPronoun().getPossessive());
            result = StringUtils.replace(result, "{player" + (i + 1) + "_self}", player.getPronoun().getSelf());

            if (player.getHp() <= 0) {
                result += " (<b>" + StringEscapeUtils.escapeHtml4(player.getEffectiveName()) + "</b> died)";
            }
        }

        return result;
    }

}
