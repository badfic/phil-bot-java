package com.badfic.philbot.data.phil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsStateJson {

    private String state;

    private String nickname;

    @JsonProperty("capital_city")
    private String capital;

    @JsonProperty("state_flag_url")
    private String stateFlagUrl;

    @JsonProperty("map_image_url")
    private String mapUrl;

    @JsonProperty("landscape_background_url")
    private String landscapeUrl;

    @JsonProperty("skyline_background_url")
    private String skylineUrl;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getStateFlagUrl() {
        return stateFlagUrl;
    }

    public void setStateFlagUrl(String stateFlagUrl) {
        this.stateFlagUrl = stateFlagUrl;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    public String getLandscapeUrl() {
        return landscapeUrl;
    }

    public void setLandscapeUrl(String landscapeUrl) {
        this.landscapeUrl = landscapeUrl;
    }

    public String getSkylineUrl() {
        return skylineUrl;
    }

    public void setSkylineUrl(String skylineUrl) {
        this.skylineUrl = skylineUrl;
    }

}
