package com.badfic.philbot.web;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.LoginRedirectException;
import com.badfic.philbot.config.UnauthorizedException;
import com.badfic.philbot.data.DiscordApiIdentityResponse;
import com.badfic.philbot.data.DiscordApiLoginResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class HomeController extends BaseController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView home(
            @RequestParam(value = "code") Optional<String> code,
            HttpServletRequest httpServletRequest) throws Exception {
        if (code.isPresent() && httpServletRequest.getSession().getAttribute(DISCORD_TOKEN) == null) {
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
                    .append(URLEncoder.encode(baseConfig.hostname, StandardCharsets.UTF_8.name()))
                    .append("&scope=identify")
                    .toString();

            LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.USER_AGENT, Constants.USER_AGENT);
            ResponseEntity<DiscordApiLoginResponse> loginResponse = restTemplate.exchange(authApi, HttpMethod.POST,
                    new HttpEntity<>(body, headers), DiscordApiLoginResponse.class);

            DiscordApiLoginResponse discordApiLoginResponse = loginResponse.getBody();
            String accessToken = discordApiLoginResponse.getAccessToken();

            DiscordApiIdentityResponse discordApiIdentityResponse = getDiscordApiIdentityResponse(accessToken);

            Member memberById = philJda.getGuilds().get(0).getMemberById(discordApiIdentityResponse.getId());
            if (memberById != null) {
                httpServletRequest.getSession().setAttribute(DISCORD_TOKEN, accessToken);
                httpServletRequest.getSession().setAttribute(DISCORD_REFRESH_TOKEN, discordApiLoginResponse.getRefreshToken());
                httpServletRequest.getSession().setAttribute(DISCORD_ID, discordApiIdentityResponse.getId());
                httpServletRequest.getSession().setAttribute(DISCORD_USERNAME, discordApiIdentityResponse.getUsername());
            } else {
                throw new UnauthorizedException(discordApiIdentityResponse.getId() +
                        " You are not authorized, you must be a member of the swamp to access this page");
            }
        }
        checkSession(httpServletRequest, false);

        Object awaitingRedirectUrl = httpServletRequest.getSession().getAttribute(AWAITING_REDIRECT_URL);
        if (awaitingRedirectUrl != null) {
            httpServletRequest.getSession().removeAttribute(AWAITING_REDIRECT_URL);
            throw new LoginRedirectException(((String) awaitingRedirectUrl));
        }

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Phil's Swamp");
        props.put("username", httpServletRequest.getSession().getAttribute(DISCORD_USERNAME));

        return new ModelAndView("index", props);
    }
}
