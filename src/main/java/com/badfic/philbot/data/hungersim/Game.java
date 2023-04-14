package com.badfic.philbot.data.hungersim;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;


@Table("hg_game")
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

    @MappedCollection
    private List<Player> players = new ArrayList<>();

    @Column
    private Integer roundCounter = 0;

    @Column("round_id")
    private Long round;

    @Column
    private List<String> currentOutcomes = new ArrayList<>();

    public Game(String name, List<Player> players, Round round) {
        this.id = SINGLETON_ID;
        this.name = name;
        this.players = players;
        this.round = round.getId();
    }

}
