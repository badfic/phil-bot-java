package com.badfic.philbot.data.swampygames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "swampy_games_config")
public class SwampyGamesConfig {
    public static final Short SINGLETON_ID = 1;

    @Id
    private Short id;

    @Column
    private String swiperAwaiting;

    @Column
    private String savedFromSwiper;

    @Column
    private String noSwipingPhrase;

    @Column
    private boolean boostAwaiting;

    @Column
    private String boostPhrase;

    @Column
    private boolean awaitingResetConfirmation;

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }
}
