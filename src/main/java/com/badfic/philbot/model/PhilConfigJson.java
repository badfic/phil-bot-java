package com.badfic.philbot.model;

import java.util.List;
import java.util.Set;

public class PhilConfigJson {

    private Set<String> channels;
    private List<String> responses;

    public Set<String> getChannels() {
        return channels;
    }

    public void setChannels(Set<String> channels) {
        this.channels = channels;
    }

    public List<String> getResponses() {
        return responses;
    }

    public void setResponses(List<String> responses) {
        this.responses = responses;
    }

}
