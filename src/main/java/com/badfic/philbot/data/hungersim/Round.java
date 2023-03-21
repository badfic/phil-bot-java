package com.badfic.philbot.data.hungersim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hg_round")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
