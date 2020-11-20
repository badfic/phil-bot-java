package com.badfic.philbot.web;

import com.badfic.philbot.config.UnauthorizedException;
import com.badfic.philbot.data.DiscordApiIdentityResponse;
import com.badfic.philbot.data.DiscordApiLoginResponse;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
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

@RestController
public class HomeController extends BaseController {

    @Resource
    private MustacheFactory mustacheFactory;

    private Mustache mustache;

    @PostConstruct
    public void init() {
        mustache = mustacheFactory.compile("index.mustache");
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> home(
            @RequestParam(value = "code") Optional<String> code,
            HttpSession httpSession) throws Exception {
        if (code.isPresent() && httpSession.getAttribute(DISCORD_TOKEN) == null) {
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
            headers.add(HttpHeaders.USER_AGENT, "swamp");
            ResponseEntity<DiscordApiLoginResponse> loginResponse = restTemplate.exchange(authApi, HttpMethod.POST,
                    new HttpEntity<>(body, headers), DiscordApiLoginResponse.class);

            DiscordApiLoginResponse discordApiLoginResponse = loginResponse.getBody();
            String accessToken = discordApiLoginResponse.getAccessToken();

            DiscordApiIdentityResponse discordApiIdentityResponse = getDiscordApiIdentityResponse(accessToken);

            Member memberById = philJda.getGuilds().get(0).getMemberById(discordApiIdentityResponse.getId());
            if (memberById != null) {
                httpSession.setAttribute(DISCORD_TOKEN, accessToken);
                httpSession.setAttribute(DISCORD_REFRESH_TOKEN, discordApiLoginResponse.getRefreshToken());
                httpSession.setAttribute(DISCORD_ID, discordApiIdentityResponse.getId());
                httpSession.setAttribute(DISCORD_USERNAME, discordApiIdentityResponse.getUsername());
            } else {
                throw new UnauthorizedException(discordApiIdentityResponse.getId() + " You are not authorized, you must be a swamp admin to access this page");
            }
        }
        checkSession(httpSession, false);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Phil's Swamp");
        props.put("username", httpSession.getAttribute(DISCORD_USERNAME));
        try (ReusableStringWriter stringWriter = ReusableStringWriter.getCurrent()) {
            mustache.execute(stringWriter, props);
            return ResponseEntity.ok(stringWriter.toString());
        }
    }
}
