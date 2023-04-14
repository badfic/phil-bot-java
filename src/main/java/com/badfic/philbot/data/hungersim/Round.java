package com.badfic.philbot.data.hungersim;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("hg_round")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Round {

    @Id
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private Boolean openingRound = Boolean.FALSE;

    @Transient
    private List<Outcome> outcomes;

    public Round(String name, String description, Boolean openingRound) {
        this.name = name;
        this.description = description;
        this.openingRound = openingRound;
    }
}
