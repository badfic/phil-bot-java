package com.badfic.philbot.data.hungersim;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import net.dv8tion.jda.api.JDA;
import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "hg_outcome")
public class Outcome {

    public static final Map<Integer, List<String>> VARIABLES = ImmutableMap.<Integer, List<String>>builder()
            .put(1, ImmutableList.of("{player1}", "{player1_subject}", "{player1_object}", "{player1_possessive}", "{player1_self}"))
            .put(2, ImmutableList.of("{player2}", "{player2_subject}", "{player2_object}", "{player2_possessive}", "{player2_self}"))
            .put(3, ImmutableList.of("{player3}", "{player3_subject}", "{player3_object}", "{player3_possessive}", "{player3_self}"))
            .put(4, ImmutableList.of("{player4}", "{player4_subject}", "{player4_object}", "{player4_possessive}", "{player4_self}"))
            .build();

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

    public Outcome() {
    }

    public Outcome(String outcomeText, Integer numPlayers, Integer player1Hp, Integer player2Hp, Integer player3Hp, Integer player4Hp) {
        this.outcomeText = outcomeText;
        this.numPlayers = Objects.requireNonNullElse(numPlayers, 1);
        this.player1Hp = Objects.requireNonNullElse(player1Hp, 0);
        this.player2Hp = Objects.requireNonNullElse(player2Hp, 0);
        this.player3Hp = Objects.requireNonNullElse(player3Hp, 0);
        this.player4Hp = Objects.requireNonNullElse(player4Hp, 0);
    }

    public Long getId() {
        return id;
    }

    public String getOutcomeText() {
        return outcomeText;
    }

    public void setOutcomeText(String text) {
        this.outcomeText = text;
    }

    public Integer getNumPlayers() {
        return numPlayers;
    }

    public void setNumPlayers(Integer numPlayers) {
        this.numPlayers = numPlayers;
    }

    public Integer getPlayer1Hp() {
        return player1Hp;
    }

    public void setPlayer1Hp(Integer player1Hp) {
        this.player1Hp = player1Hp;
    }

    public Integer getPlayer2Hp() {
        return player2Hp;
    }

    public void setPlayer2Hp(Integer player2Hp) {
        this.player2Hp = player2Hp;
    }

    public Integer getPlayer3Hp() {
        return player3Hp;
    }

    public void setPlayer3Hp(Integer player3Hp) {
        this.player3Hp = player3Hp;
    }

    public Integer getPlayer4Hp() {
        return player4Hp;
    }

    public void setPlayer4Hp(Integer player4Hp) {
        this.player4Hp = player4Hp;
    }

    public String apply(Player player1, JDA jda) {
        player1.setHp(player1.getHp() + player1Hp);

        List<Player> playerList = ImmutableList.of(player1);
        return getOutcomeResultString(jda, playerList);
    }

    public String apply(Player player1, Player player2, JDA jda) {
        player1.setHp(player1.getHp() + player1Hp);
        player2.setHp(player2.getHp() + player2Hp);

        List<Player> playerList = ImmutableList.of(player1, player2);
        return getOutcomeResultString(jda, playerList);
    }

    public String apply(Player player1, Player player2, Player player3, JDA jda) {
        player1.setHp(player1.getHp() + player1Hp);
        player2.setHp(player2.getHp() + player2Hp);
        player3.setHp(player3.getHp() + player3Hp);

        List<Player> playerList = ImmutableList.of(player1, player2, player3);
        return getOutcomeResultString(jda, playerList);
    }

    public String apply(Player player1, Player player2, Player player3, Player player4, JDA jda) {
        player1.setHp(player1.getHp() + player1Hp);
        player2.setHp(player2.getHp() + player2Hp);
        player3.setHp(player3.getHp() + player3Hp);
        player4.setHp(player4.getHp() + player4Hp);

        List<Player> playerList = ImmutableList.of(player1, player2, player3, player4);
        return getOutcomeResultString(jda, playerList);
    }

    private String getOutcomeResultString(JDA jda, List<Player> playerList) {
        String result = outcomeText;

        for (int i = 0; i < playerList.size(); i++) {
            Player player = playerList.get(i);

            player.setEffectiveNameViaJda(jda);

            result = StringUtils.replace(result, "{player" + (i + 1) + '}', player.getEffectiveName());
            result = StringUtils.replace(result, "{player" + (i + 1) + "_subject}", player.getPronoun().getSubject());
            result = StringUtils.replace(result, "{player" + (i + 1) + "_object}", player.getPronoun().getObject());
            result = StringUtils.replace(result, "{player" + (i + 1) + "_possessive}", player.getPronoun().getPossessive());
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Outcome outcome = (Outcome) o;
        return Objects.equals(id, outcome.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
