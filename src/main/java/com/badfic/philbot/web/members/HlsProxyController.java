package com.badfic.philbot.web.members;

import com.badfic.philbot.config.NewSessionException;
import com.badfic.philbot.config.UnauthorizedException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HlsProxyController extends BaseMembersController {

    private static final Cache<String, String> OUTSTANDING_AUTHS = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.of(1, ChronoUnit.DAYS))
            .build();

    @GetMapping(value = "/{authCode}/hls/**")
    public ResponseEntity<byte[]> hlsProxy(HttpServletRequest httpServletRequest, @PathVariable("authCode") String auth) {
        HttpSession httpSession = httpServletRequest.getSession();

        if (httpSession.getAttribute(CHROMECAST_AUTH) == null && OUTSTANDING_AUTHS.getIfPresent(auth) != null) {
            httpSession.setAttribute(CHROMECAST_AUTH, auth);
        }
        if (httpSession.getAttribute(CHROMECAST_AUTH) == null) {
            throw new UnauthorizedException("You do not have a valid session. Please refresh and login again");
        }

        String targetUrl = "http://" + baseConfig.owncastInstance + httpServletRequest.getRequestURI().replace("/" + auth, "");
        return restTemplate.getForEntity(targetUrl, byte[].class);
    }

    @GetMapping(value = "/chromecast-auth")
    public ResponseEntity<String> chromecastAuth(HttpServletRequest httpServletRequest) {
        HttpSession httpSession = httpServletRequest.getSession();
        if (httpSession.isNew()) {
            throw new NewSessionException();
        }
        if (httpSession.getAttribute(DISCORD_TOKEN) == null) {
            throw new UnauthorizedException("You do not have a valid session. Please refresh and login again");
        }

        String auth = UUID.randomUUID().toString();
        OUTSTANDING_AUTHS.put(auth, auth);
        return ResponseEntity.ok(auth);
    }

}