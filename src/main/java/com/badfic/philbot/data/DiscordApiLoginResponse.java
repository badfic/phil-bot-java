package com.badfic.philbot.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DiscordApiLoginResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("refresh_token") String refreshToken,
                                      @JsonProperty("expires_in") Long expiresIn) {
}
