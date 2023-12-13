package com.badfic.philbot.web.members;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.LoginRedirectException;
import com.badfic.philbot.config.UnauthorizedException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController extends BaseMembersController {

    @GetMapping(value = {"", "/", "/home"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView home(
            @RequestParam(value = "code") Optional<String> code,
            HttpServletRequest httpServletRequest) throws Exception {
        HttpSession session = httpServletRequest.getSession();

        if (code.isPresent() && session.getAttribute(DISCORD_TOKEN) == null) {
            DiscordApiLoginResponse discordApiLoginResponse = getDiscordApiLoginResponse(code);

            session.setAttribute(DISCORD_TOKEN, discordApiLoginResponse.accessToken());
            session.setAttribute(DISCORD_TOKEN_EXPIRES_AT, LocalDateTime.now().plusSeconds(discordApiLoginResponse.expiresIn()));
            session.setAttribute(DISCORD_REFRESH_TOKEN, discordApiLoginResponse.refreshToken());

            DiscordApiIdentityResponse discordApiIdentityResponse = getDiscordApiIdentityResponse(discordApiLoginResponse.accessToken());

            Member memberById = philJda.getGuildById(baseConfig.guildId).getMemberById(discordApiIdentityResponse.id());
            if (memberById != null) {
                Boolean isMod = hasRole(memberById, Constants.ADMIN_ROLE);
                Boolean is18 = hasRole(memberById, Constants.EIGHTEEN_PLUS_ROLE);

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

        Object awaitingRedirectUrl = session.getAttribute(AWAITING_REDIRECT_URL);
        if (awaitingRedirectUrl != null) {
            session.removeAttribute(AWAITING_REDIRECT_URL);
            throw new LoginRedirectException(((String) awaitingRedirectUrl));
        }

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Phil's Swamp");
        addCommonProps(session, props);

        return new ModelAndView("members-home", props);
    }

    private DiscordApiLoginResponse getDiscordApiLoginResponse(Optional<String> code) {
        String authApi = "https://discordapp.com/api/oauth2/token";
        String body = new StringBuilder()
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

        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);

        ResponseEntity<DiscordApiLoginResponse> loginResponse = restTemplate.exchange(authApi, HttpMethod.POST,
                new HttpEntity<>(body, headers), DiscordApiLoginResponse.class);

        return loginResponse.getBody();
    }

    private DiscordApiIdentityResponse getDiscordApiIdentityResponse(String accessToken) {
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        ResponseEntity<DiscordApiIdentityResponse> identityResponse = restTemplate.exchange("https://discord.com/api/users/@me", HttpMethod.GET,
                new HttpEntity<>(headers), DiscordApiIdentityResponse.class);

        return identityResponse.getBody();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DiscordApiIdentityResponse(String id, String username) {}

}
