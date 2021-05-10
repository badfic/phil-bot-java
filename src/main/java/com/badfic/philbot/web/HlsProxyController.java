package com.badfic.philbot.web;

import com.badfic.philbot.config.NewSessionException;
import com.badfic.philbot.config.UnauthorizedException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HlsProxyController extends BaseController {

    private static final Cache<String, String> OUTSTANDING_AUTHS = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.of(15, ChronoUnit.MINUTES))
            .build();

    @GetMapping(value = "/hls/**")
    public ResponseEntity<byte[]> hlsProxy(HttpServletRequest httpServletRequest, @RequestParam("auth") Optional<String> auth) {
        HttpSession httpSession = httpServletRequest.getSession();

        if (auth.isPresent()) {
            if (OUTSTANDING_AUTHS.getIfPresent(auth.get()) != null) {
                httpSession.setAttribute(CHROMECAST_AUTH, auth.get());
            }
        }
        if (httpSession.getAttribute(CHROMECAST_AUTH) == null) {
            throw new UnauthorizedException("You do not have a valid session. Please refresh and login again");
        }

        return restTemplate.getForEntity("http://" + baseConfig.owncastInstance + httpServletRequest.getRequestURI(), byte[].class);
    }

    @GetMapping(value = "chromecast-auth")
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