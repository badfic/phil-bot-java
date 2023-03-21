package com.badfic.philbot.web.members;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MemeForm(String memeName, String memeUrl) {
}
