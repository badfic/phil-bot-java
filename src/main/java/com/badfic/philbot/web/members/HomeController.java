package com.badfic.philbot.web.members;

import com.badfic.philbot.config.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController extends BaseMembersController {

    @GetMapping(value = {"", "/", "/home"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView home(
            @RequestParam(value = "code") final Optional<String> code,
            final HttpServletRequest httpServletRequest) throws Exception {
        final var session = httpServletRequest.getSession();

        if (code.isPresent() && session.getAttribute(DISCORD_TOKEN) == null) {
            final var discordApiLoginResponse = getDiscordApiLoginResponse(code);

            session.setAttribute(DISCORD_TOKEN, discordApiLoginResponse.accessToken());
            session.setAttribute(DISCORD_TOKEN_EXPIRES_AT, LocalDateTime.now().plusSeconds(discordApiLoginResponse.expiresIn()));
            session.setAttribute(DISCORD_REFRESH_TOKEN, discordApiLoginResponse.refreshToken());

            final var discordApiIdentityResponse = getDiscordApiIdentityResponse(discordApiLoginResponse.accessToken());

            final var memberById = philJda.getGuildById(baseConfig.guildId).getMemberById(discordApiIdentityResponse.id());
            if (memberById != null) {
                final var isMod = hasRole(memberById, Constants.ADMIN_ROLE);
                final var is18 = hasRole(memberById, Constants.EIGHTEEN_PLUS_ROLE);

                session.setAttribute(DISCORD_ID, discordApiIdentityResponse.id());
                session.setAttribute(DISCORD_USERNAME, discordApiIdentityResponse.username());
                session.setAttribute(DISCORD_IS_MOD, isMod);
                session.setAttribute(DISCORD_IS_18, is18);
            } else {
                throw new UnauthorizedException(discordApiIdentityResponse.id() +
                        " You are not authorized, you must be a member of the swamp to access this page");
            }
        }
        checkSession(httpServletRequest, false);

        final var awaitingRedirectUrl = session.getAttribute(AWAITING_REDIRECT_URL);
        if (awaitingRedirectUrl != null) {
            session.removeAttribute(AWAITING_REDIRECT_URL);
            throw new LoginRedirectException(((String) awaitingRedirectUrl));
        }

        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Phil's Swamp");
        addCommonProps(session, props);

        return new ModelAndView("members-home", props);
    }

    private DiscordApiLoginResponse getDiscordApiLoginResponse(final Optional<String> code) {
        final var authApi = "https://discordapp.com/api/oauth2/token";
        final var body = new StringBuilder()
                .append("client_id=")
                .append(baseConfig.discordClientId)
                .append("&client_secret=")
                .append(baseConfig.discordClientSecret)
                .append("&grant_type=authorization_code")
                .append("&code=")
                .append(code.get())
                .append("&redirect_uri=")
                .append(URLEncoder.encode(baseConfig.hostname + "/members/home", StandardCharsets.UTF_8))
                .append("&scope=identify")
                .toString();

        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);

        final var loginResponse = restTemplate.exchange(authApi, HttpMethod.POST, new HttpEntity<>(body, headers), DiscordApiLoginResponse.class);

        return loginResponse.getBody();
    }

    private DiscordApiIdentityResponse getDiscordApiIdentityResponse(final String accessToken) {
        final var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(accessToken);
        headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);

        final var identityResponse = restTemplate.exchange("https://discord.com/api/users/@me", HttpMethod.GET,
                new HttpEntity<>(headers), DiscordApiIdentityResponse.class);

        return identityResponse.getBody();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DiscordApiIdentityResponse(String id, String username) {}

}
