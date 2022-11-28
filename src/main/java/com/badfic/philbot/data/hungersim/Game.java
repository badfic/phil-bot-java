package com.badfic.philbot.data.hungersim;

import com.badfic.philbot.data.JsonListConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "hg_game")
public class Game {

    public static final Short SINGLETON_ID = 1;

    @Id
    @JsonIgnore
    private Short id;

    @Column
    private String name;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private List<Player> players = new ArrayList<>();

    @Column
    private Integer roundCounter = 0;

    @OneToOne
    private Round round;

    @Column
    @Convert(converter = JsonListConverter.class)
    private List<String> currentOutcomes = new ArrayList<>();

    public Game() {
    }

    public Game(String name, List<Player> players, Round round) {
        this.id = SINGLETON_ID;
        this.name = name;
        this.players = players;
        this.round = round;
    }

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Integer getRoundCounter() {
        return roundCounter;
    }

    public void setRoundCounter(Integer roundCounter) {
        this.roundCounter = roundCounter;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round currentRound) {
        this.round = currentRound;
    }

    public List<String> getCurrentOutcomes() {
        return currentOutcomes;
    }

    public void setCurrentOutcomes(List<String> currentOutcomes) {
        this.currentOutcomes = currentOutcomes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Game game = (Game) o;
        return Objects.equals(id, game.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
