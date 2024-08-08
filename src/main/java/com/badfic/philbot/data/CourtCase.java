package com.badfic.philbot.data;

import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("court_case")
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

    public CourtCase(final long defendantId, final long accuserId, final long trialMessageId, final String crime, final LocalDateTime trialDate) {
        this.defendantId = defendantId;
        this.accuserId = accuserId;
        this.trialMessageId = trialMessageId;
        this.crime = crime;
        this.trialDate = trialDate;
    }

}
