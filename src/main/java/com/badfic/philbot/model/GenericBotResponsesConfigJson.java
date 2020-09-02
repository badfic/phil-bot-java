package com.badfic.philbot.model;

import java.util.Objects;
import java.util.Set;

public class GenericBotResponsesConfigJson {

    private Set<String> channels;
    private Set<String> responses;

    public Set<String> getChannels() {
        return channels;
    }

    public void setChannels(Set<String> channels) {
        this.channels = channels;
    }

    public Set<String> getResponses() {
        return responses;
    }

    public void setResponses(Set<String> responses) {
        this.responses = responses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericBotResponsesConfigJson that = (GenericBotResponsesConfigJson) o;
        return Objects.equals(channels, that.channels) &&
                Objects.equals(responses, that.responses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channels, responses);
    }

}
