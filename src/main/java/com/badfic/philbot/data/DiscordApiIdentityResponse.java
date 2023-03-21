package com.badfic.philbot.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DiscordApiIdentityResponse(String id, String username) {
}
