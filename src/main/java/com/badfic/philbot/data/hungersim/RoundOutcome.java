package com.badfic.philbot.data.hungersim;


import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("hg_round_outcome")
public class RoundOutcome {

    @Id
    private Long id;

    @Column("round_id")
    private Long round;

    @Column("outcome_id")
    private Long outcome;

    public RoundOutcome() {
    }

    public RoundOutcome(Round round, Outcome outcome) {
        this.round = round.getId();
        this.outcome = outcome.getId();
    }

    public Long getId() {
        return id;
    }

    public Long getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round.getId();
    }

    public Long getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoundOutcome that = (RoundOutcome) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
