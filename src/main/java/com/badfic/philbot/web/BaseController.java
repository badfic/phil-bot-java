package com.badfic.philbot.web;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.data.DiscordApiIdentityResponse;
import com.badfic.philbot.data.DiscordApiLoginResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import net.dv8tion.jda.api.JDA;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

public abstract class BaseController {
    protected static final String DISCORD_TOKEN = "DiscordToken";
    protected static final String DISCORD_ID = "DiscordId";
    protected static final String DISCORD_USERNAME = "DiscordUsername";
    protected static final String DISCORD_REFRESH_TOKEN = "DiscordRefreshToken";

    @Resource
    protected BaseConfig baseConfig;

    @Resource
    protected RestTemplate restTemplate;

    @Resource(name = "philJda")
    protected JDA philJda;

    protected ResponseEntity<String> redirectDiscordLogin() throws UnsupportedEncodingException {
        StringBuilder urlBuilder = new StringBuilder()
                .append("https://discord.com/api/oauth2/authorize?client_id=")
                .append(baseConfig.discordClientId)
                .append("&redirect_uri=")
                .append(URLEncoder.encode(baseConfig.hostname, StandardCharsets.UTF_8.name()))
                .append("&response_type=code&scope=identify");
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, urlBuilder.toString()).build();
    }

    protected DiscordApiIdentityResponse getDiscordApiIdentityResponse(String accessToken) {
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.USER_AGENT, "swamp");
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        ResponseEntity<DiscordApiIdentityResponse> identityResponse = restTemplate.exchange("https://discord.com/api/users/@me", HttpMethod.GET,
                new HttpEntity<>(headers), DiscordApiIdentityResponse.class);

        return identityResponse.getBody();
    }

    protected void refreshTokenIfNeeded(HttpSession httpSession) throws UnsupportedEncodingException {
        try {
            Objects.requireNonNull(getDiscordApiIdentityResponse((String) httpSession.getAttribute(DISCORD_TOKEN)));
        } catch (Exception e) {
            String authApi = "https://discordapp.com/api/oauth2/token";
            String body = new StringBuilder()
                    .append("client_id=")
                    .append(baseConfig.discordClientId)
                    .append("&client_secret=")
                    .append(baseConfig.discordClientSecret)
                    .append("&grant_type=refresh_token")
                    .append("&refresh_token=")
                    .append((String) httpSession.getAttribute(DISCORD_REFRESH_TOKEN))
                    .append("&redirect_uri=")
                    .append(URLEncoder.encode(baseConfig.hostname, StandardCharsets.UTF_8.name()))
                    .append(URLEncoder.encode("/trivia", StandardCharsets.UTF_8.name()))
                    .append("&scope=identify")
                    .toString();

            LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.USER_AGENT, "swamp");
            ResponseEntity<DiscordApiLoginResponse> loginResponse = restTemplate.exchange(authApi, HttpMethod.POST,
                    new HttpEntity<>(body, headers), DiscordApiLoginResponse.class);
            httpSession.setAttribute(DISCORD_TOKEN, loginResponse.getBody().getAccessToken());
            httpSession.setAttribute(DISCORD_REFRESH_TOKEN, loginResponse.getBody().getRefreshToken());
        }
    }

}
