package com.badfic.philbot.data.phil;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "timeout_case")
public class TimeoutCase {

    @Id
    private long userId;

    @Column
    private LocalDateTime releaseDate;

    public TimeoutCase() {
    }

    public TimeoutCase(long userId, LocalDateTime releaseDate) {
        this.userId = userId;
        this.releaseDate = releaseDate;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

}
