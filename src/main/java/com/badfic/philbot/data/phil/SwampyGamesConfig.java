package com.badfic.philbot.data.phil;

import com.badfic.philbot.data.JsonSetConverter;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Convert;
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
    private String swiperSavior;

    @Column
    private String noSwipingPhrase;

    @Column
    private String boostPhrase;

    @Column
    private int storyTimeCounter;

    @Column
    private String jigsawAwaiting;

    @Column
    private String jigsawMessageId;

    @Column
    @Convert(converter = JsonSetConverter.class)
    private Set<String> pastVictims = new HashSet<>();

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public String getSwiperAwaiting() {
        return swiperAwaiting;
    }

    public void setSwiperAwaiting(String swiperAwaiting) {
        this.swiperAwaiting = swiperAwaiting;
    }

    public String getSwiperSavior() {
        return swiperSavior;
    }

    public void setSwiperSavior(String swiperSavior) {
        this.swiperSavior = swiperSavior;
    }

    public String getNoSwipingPhrase() {
        return noSwipingPhrase;
    }

    public void setNoSwipingPhrase(String noSwipingPhrase) {
        this.noSwipingPhrase = noSwipingPhrase;
    }

    public String getBoostPhrase() {
        return boostPhrase;
    }

    public void setBoostPhrase(String boostPhrase) {
        this.boostPhrase = boostPhrase;
    }

    public int getStoryTimeCounter() {
        return storyTimeCounter;
    }

    public void setStoryTimeCounter(int storyTimeCounter) {
        this.storyTimeCounter = storyTimeCounter;
    }

    public String getJigsawAwaiting() {
        return jigsawAwaiting;
    }

    public void setJigsawAwaiting(String jigsawAwaiting) {
        this.jigsawAwaiting = jigsawAwaiting;
    }

    public String getJigsawMessageId() {
        return jigsawMessageId;
    }

    public void setJigsawMessageId(String jigsawMessageId) {
        this.jigsawMessageId = jigsawMessageId;
    }

    public Set<String> getPastVictims() {
        return pastVictims;
    }

    public void setPastVictims(Set<String> pastVictims) {
        this.pastVictims = pastVictims;
    }
}
