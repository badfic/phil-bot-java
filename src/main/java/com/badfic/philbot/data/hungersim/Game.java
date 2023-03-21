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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hg_game")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
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

    public Game(String name, List<Player> players, Round round) {
        this.id = SINGLETON_ID;
        this.name = name;
        this.players = players;
        this.round = round;
    }

}
