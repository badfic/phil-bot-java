package com.badfic.philbot.data.hungersim;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("hg_game")
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

    @Column
    private Integer roundCounter = 0;

    @Column("round_id")
    private Long round;

    @Column
    private String[] currentOutcomes = {};

    @Transient
    private List<Player> players;

}
