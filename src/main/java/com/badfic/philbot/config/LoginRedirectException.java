package com.badfic.philbot.config;

public class LoginRedirectException extends RuntimeException {
    private final String url;

    public LoginRedirectException(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
