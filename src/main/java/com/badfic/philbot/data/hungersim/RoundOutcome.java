package com.badfic.philbot.data.hungersim;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("hg_round_outcome")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class RoundOutcome {
    @Id
    @Getter
    private Long id;

    @Column("round_id")
    @Getter
    private Long round;

    @Column("outcome_id")
    @Getter
    private Long outcome;

    public RoundOutcome(final Round round, final Outcome outcome) {
        this.round = round.getId();
        this.outcome = outcome.getId();
    }

    public void setRound(final Round round) {
        this.round = round.getId();
    }

    public void setOutcome(final Outcome outcome) {
        this.outcome = outcome.getId();
    }
}
