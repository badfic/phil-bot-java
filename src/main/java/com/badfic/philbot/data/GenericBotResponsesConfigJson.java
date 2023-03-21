package com.badfic.philbot.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GenericBotResponsesConfigJson(Set<String> channels, Set<String> responses) {
}
