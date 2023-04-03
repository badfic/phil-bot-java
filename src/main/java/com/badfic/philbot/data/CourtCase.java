package com.badfic.philbot.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "court_case")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "defendantId")
public class CourtCase {
    @Id
    private long defendantId;

    @Column
    private long accuserId;

    @Column
    private long trialMessageId;

    @Column
    private String crime;

    @Column
    private LocalDateTime trialDate;

    @Column
    private LocalDateTime releaseDate;

    public CourtCase(long defendantId, long accuserId, long trialMessageId, String crime, LocalDateTime trialDate) {
        this.defendantId = defendantId;
        this.accuserId = accuserId;
        this.trialMessageId = trialMessageId;
        this.crime = crime;
        this.trialDate = trialDate;
    }

}
