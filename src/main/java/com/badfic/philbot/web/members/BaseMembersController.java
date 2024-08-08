package com.badfic.philbot.web.members;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUserRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@RequestMapping("/members")
public abstract class BaseMembersController {
    protected static final String DISCORD_TOKEN = "DiscordToken";
    protected static final String DISCORD_REFRESH_TOKEN = "DiscordRefreshToken";
    protected static final String DISCORD_TOKEN_EXPIRES_AT = "DiscordTokenExpiresAt";
    protected static final String DISCORD_ID = "DiscordId";
    protected static final String DISCORD_USERNAME = "DiscordUsername";
    protected static final String DISCORD_IS_MOD = "DiscordIsMod";
    protected static final String DISCORD_IS_18 = "DiscordIs18";
    protected static final String AWAITING_REDIRECT_URL = "AwaitingRedirectUrl";

    @Setter(onMethod_ = {@Autowired})
    protected BaseConfig baseConfig;

    @Setter(onMethod_ = {@Autowired})
    protected ObjectMapper objectMapper;

    @Setter(onMethod_ = {@Autowired})
    protected RestTemplate restTemplate;

    @Setter(onMethod_ = {@Autowired})
    protected DiscordUserRepository discordUserRepository;

    @Setter(onMethod_ = {@Autowired})
    protected JDA philJda;

    protected void checkSession(final HttpServletRequest httpServletRequest, final boolean requiresAdmin) throws UnsupportedEncodingException {
        final var session = httpServletRequest.getSession();
        if (session.isNew()) {
            session.setAttribute(AWAITING_REDIRECT_URL, httpServletRequest.getRequestURI());
            throw new NewSessionException();
        }

        final var discordId = (String) session.getAttribute(DISCORD_ID);
        if (StringUtils.isBlank(discordId)) {
            throw new UnauthorizedException("You do not have a valid session. Please refresh and login again");
        }

        final var memberById = philJda.getGuildById(baseConfig.guildId).getMemberById(discordId);
        if (memberById == null || (requiresAdmin && !hasRole(memberById, Constants.ADMIN_ROLE))) {
            throw new UnauthorizedException(discordId + " You are not authorized, you must be a swamp "
                    + (requiresAdmin ? "admin" : "member")
                    + " to access this page");
        }

        refreshTokenIfNeeded(httpServletRequest);
    }

    protected boolean hasRole(final Member member, final String role) {
        return member.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(role));
    }

    protected void addCommonProps(final HttpSession session, final Map<String, Object> props) {
        props.put("username", session.getAttribute(DISCORD_USERNAME));
        props.put("isMod", session.getAttribute(DISCORD_IS_MOD));
        props.put("is18", session.getAttribute(DISCORD_IS_18));
    }

    private void refreshTokenIfNeeded(final HttpServletRequest httpServletRequest) {
        final var session = httpServletRequest.getSession();
        try {
            final var expiresAt = ((LocalDateTime) session.getAttribute(DISCORD_TOKEN_EXPIRES_AT));

            if (LocalDateTime.now().plusHours(1).isAfter(expiresAt)) {
                refreshToken(session);
            }
        } catch (final UnauthorizedException e) {
            throw e;
        } catch (final Exception e) {
            final var it = session.getAttributeNames().asIterator();
            while (it.hasNext()) {
                final var name = it.next();
                session.removeAttribute(name);
            }
            session.setAttribute(AWAITING_REDIRECT_URL, httpServletRequest.getRequestURI());

            throw new NewSessionException();
        }
    }

    private void refreshToken(final HttpSession session) {
        final var authApi = "https://discordapp.com/api/oauth2/token";
        final var body = new StringBuilder()
                .append("client_id=")
                .append(baseConfig.discordClientId)
                .append("&client_secret=")
                .append(baseConfig.discordClientSecret)
                .append("&grant_type=refresh_token")
                .append("&refresh_token=")
                .append(session.getAttribute(DISCORD_REFRESH_TOKEN))
                .toString();

        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);

        final var loginResponse = restTemplate.exchange(authApi, HttpMethod.POST, new HttpEntity<>(body, headers), DiscordApiLoginResponse.class);

        final var discordApiLoginResponse = loginResponse.getBody();

        session.setAttribute(DISCORD_TOKEN, discordApiLoginResponse.accessToken());
        session.setAttribute(DISCORD_TOKEN_EXPIRES_AT, LocalDateTime.now().plusSeconds(discordApiLoginResponse.expiresIn()));
        session.setAttribute(DISCORD_REFRESH_TOKEN, discordApiLoginResponse.refreshToken());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DiscordApiLoginResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("refresh_token") String refreshToken,
                                          @JsonProperty("expires_in") Long expiresIn) {}

    @Getter
    @RequiredArgsConstructor
    public static class LoginRedirectException extends RuntimeException {
        private final String url;
    }

    public static class NewSessionException extends RuntimeException {}

    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(final String message) {
            super(message);
        }
    }
}
