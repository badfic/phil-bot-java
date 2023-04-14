package com.badfic.philbot.web.members;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.NewSessionException;
import com.badfic.philbot.config.UnauthorizedException;
import com.badfic.philbot.data.DiscordUserRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@RequestMapping("/members/")
public abstract class BaseMembersController {
    protected static final String DISCORD_TOKEN = "DiscordToken";
    protected static final String DISCORD_REFRESH_TOKEN = "DiscordRefreshToken";
    protected static final String DISCORD_TOKEN_EXPIRES_AT = "DiscordTokenExpiresAt";
    protected static final String DISCORD_ID = "DiscordId";
    protected static final String DISCORD_USERNAME = "DiscordUsername";
    protected static final String DISCORD_IS_MOD = "DiscordIsMod";
    protected static final String DISCORD_IS_18 = "DiscordIs18";
    protected static final String AWAITING_REDIRECT_URL = "AwaitingRedirectUrl";
    protected static final String CHROMECAST_AUTH = "ChromecastAuth";

    @Setter(onMethod_ = {@Autowired})
    protected BaseConfig baseConfig;

    @Setter(onMethod_ = {@Autowired})
    protected ObjectMapper objectMapper;

    @Setter(onMethod_ = {@Autowired})
    protected RestTemplate restTemplate;

    @Setter(onMethod_ = {@Autowired})
    protected DiscordUserRepository discordUserRepository;

    @Setter(onMethod_ = {@Autowired, @Qualifier("philJda"), @Lazy})
    protected JDA philJda;

    protected void checkSession(HttpServletRequest httpServletRequest, boolean requiresAdmin) throws UnsupportedEncodingException {
        HttpSession session = httpServletRequest.getSession();
        if (session.isNew()) {
            session.setAttribute(AWAITING_REDIRECT_URL, httpServletRequest.getRequestURI());
            throw new NewSessionException();
        }

        String discordId = (String) session.getAttribute(DISCORD_ID);
        if (StringUtils.isBlank(discordId)) {
            throw new UnauthorizedException("You do not have a valid session. Please refresh and login again");
        }

        Member memberById = philJda.getGuildById(baseConfig.guildId).getMemberById(discordId);
        if (memberById == null || (requiresAdmin && !hasRole(memberById, Constants.ADMIN_ROLE))) {
            throw new UnauthorizedException(discordId + " You are not authorized, you must be a swamp "
                    + (requiresAdmin ? "admin" : "member")
                    + " to access this page");
        }

        refreshTokenIfNeeded(httpServletRequest);
    }

    protected boolean hasRole(Member member, String role) {
        return member.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(role));
    }

    protected void addCommonProps(HttpSession session, Map<String, Object> props) {
        props.put("username", session.getAttribute(DISCORD_USERNAME));
        props.put("isMod", session.getAttribute(DISCORD_IS_MOD));
        props.put("is18", session.getAttribute(DISCORD_IS_18));
    }

    protected Member getMemberFromSession(HttpSession session) {
        String discordId = (String) session.getAttribute(DISCORD_ID);
        return philJda.getGuildById(baseConfig.guildId).getMemberById(discordId);
    }

    private void refreshTokenIfNeeded(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession();
        try {
            LocalDateTime expiresAt = ((LocalDateTime) session.getAttribute(DISCORD_TOKEN_EXPIRES_AT));

            if (LocalDateTime.now().plusHours(1).isAfter(expiresAt)) {
                refreshToken(session);
            }
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            for (Iterator<String> it = session.getAttributeNames().asIterator(); it.hasNext(); ) {
                String name = it.next();
                session.removeAttribute(name);
            }
            session.setAttribute(AWAITING_REDIRECT_URL, httpServletRequest.getRequestURI());

            throw new NewSessionException();
        }
    }

    private void refreshToken(HttpSession session) {
        String authApi = "https://discordapp.com/api/oauth2/token";
        String body = new StringBuilder()
                .append("client_id=")
                .append(baseConfig.discordClientId)
                .append("&client_secret=")
                .append(baseConfig.discordClientSecret)
                .append("&grant_type=refresh_token")
                .append("&refresh_token=")
                .append(session.getAttribute(DISCORD_REFRESH_TOKEN))
                .toString();

        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);

        ResponseEntity<DiscordApiLoginResponse> loginResponse = restTemplate.exchange(authApi, HttpMethod.POST,
                new HttpEntity<>(body, headers), DiscordApiLoginResponse.class);

        DiscordApiLoginResponse discordApiLoginResponse = loginResponse.getBody();

        session.setAttribute(DISCORD_TOKEN, discordApiLoginResponse.accessToken());
        session.setAttribute(DISCORD_TOKEN_EXPIRES_AT, LocalDateTime.now().plusSeconds(discordApiLoginResponse.expiresIn()));
        session.setAttribute(DISCORD_REFRESH_TOKEN, discordApiLoginResponse.refreshToken());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DiscordApiLoginResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("refresh_token") String refreshToken,
                                          @JsonProperty("expires_in") Long expiresIn) {}

}
