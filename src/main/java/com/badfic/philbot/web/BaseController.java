package com.badfic.philbot.web;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.NewSessionException;
import com.badfic.philbot.config.UnauthorizedException;
import com.badfic.philbot.data.DiscordApiIdentityResponse;
import com.badfic.philbot.data.DiscordApiLoginResponse;
import com.badfic.philbot.data.DiscordUserRepository;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

public abstract class BaseController {
    protected static final String DISCORD_TOKEN = "DiscordToken";
    protected static final String DISCORD_ID = "DiscordId";
    protected static final String DISCORD_USERNAME = "DiscordUsername";
    protected static final String DISCORD_REFRESH_TOKEN = "DiscordRefreshToken";
    protected static final String AWAITING_REDIRECT_URL = "AwaitingRedirectUrl";
    protected static final String CHROMECAST_AUTH = "ChromecastAuth";

    @Resource
    protected BaseConfig baseConfig;

    @Resource
    protected RestTemplate restTemplate;

    @Resource
    protected DiscordUserRepository discordUserRepository;

    @Resource(name = "philJda")
    protected JDA philJda;

    protected void checkSession(HttpServletRequest httpServletRequest, boolean requiresAdmin) throws UnsupportedEncodingException {
        if (httpServletRequest.getSession().isNew()) {
            httpServletRequest.getSession().setAttribute(AWAITING_REDIRECT_URL, httpServletRequest.getRequestURI());
            throw new NewSessionException();
        }
        if (httpServletRequest.getSession().getAttribute(DISCORD_TOKEN) == null) {
            throw new UnauthorizedException("You do not have a valid session. Please refresh and login again");
        }
        refreshTokenIfNeeded(httpServletRequest.getSession(), requiresAdmin);
    }

    protected DiscordApiIdentityResponse getDiscordApiIdentityResponse(String accessToken) {
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        ResponseEntity<DiscordApiIdentityResponse> identityResponse = restTemplate.exchange("https://discord.com/api/users/@me", HttpMethod.GET,
                new HttpEntity<>(headers), DiscordApiIdentityResponse.class);

        return identityResponse.getBody();
    }

    protected boolean hasRole(Member member, String role) {
        return member.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(role));
    }

    private void refreshTokenIfNeeded(HttpSession httpSession, boolean requiresAdmin) throws UnsupportedEncodingException {
        try {
            DiscordApiIdentityResponse discordApiIdentityResponse = Objects.requireNonNull(
                    getDiscordApiIdentityResponse((String) httpSession.getAttribute(DISCORD_TOKEN)));

            Member memberById = philJda.getGuilds().get(0).getMemberById(discordApiIdentityResponse.getId());
            if (memberById == null || (requiresAdmin && !hasRole(memberById, Constants.ADMIN_ROLE) && !hasRole(memberById, Constants.MOD_ROLE))) {
                throw new UnauthorizedException(discordApiIdentityResponse.getId() +
                        " You are not authorized, you must be a swamp " + (requiresAdmin ? "admin" : "member") + " to access this page");
            }
        } catch (UnauthorizedException e) {
            throw e;
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
            headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);
            ResponseEntity<DiscordApiLoginResponse> loginResponse = restTemplate.exchange(authApi, HttpMethod.POST,
                    new HttpEntity<>(body, headers), DiscordApiLoginResponse.class);
            httpSession.setAttribute(DISCORD_TOKEN, loginResponse.getBody().getAccessToken());
            httpSession.setAttribute(DISCORD_REFRESH_TOKEN, loginResponse.getBody().getRefreshToken());
        }
    }

}
