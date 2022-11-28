package com.badfic.philbot.data.phil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "court_case")
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

    public CourtCase() {
    }

    public CourtCase(long defendantId, long accuserId, long trialMessageId, String crime, LocalDateTime trialDate) {
        this.defendantId = defendantId;
        this.accuserId = accuserId;
        this.trialMessageId = trialMessageId;
        this.crime = crime;
        this.trialDate = trialDate;
    }

    public long getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(long defendantId) {
        this.defendantId = defendantId;
    }

    public long getAccuserId() {
        return accuserId;
    }

    public void setAccuserId(long accuserId) {
        this.accuserId = accuserId;
    }

    public long getTrialMessageId() {
        return trialMessageId;
    }

    public void setTrialMessageId(long trialMessageId) {
        this.trialMessageId = trialMessageId;
    }

    public String getCrime() {
        return crime;
    }

    public void setCrime(String crime) {
        this.crime = crime;
    }

    public LocalDateTime getTrialDate() {
        return trialDate;
    }

    public void setTrialDate(LocalDateTime trialDate) {
        this.trialDate = trialDate;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }
}
