package com.badfic.philbot.web.members;

import com.badfic.philbot.config.NewSessionException;
import com.badfic.philbot.config.UnauthorizedException;
import com.badfic.philbot.service.DailyTickable;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HlsProxyController extends BaseMembersController implements DailyTickable {

    private static final Object2LongMap<String> OUTSTANDING_AUTHS = Object2LongMaps.synchronize(new Object2LongArrayMap<>());

    @GetMapping(value = "/{authCode}/hls/**")
    public ResponseEntity<byte[]> hlsProxy(HttpServletRequest httpServletRequest, @PathVariable("authCode") String auth) {
        HttpSession httpSession = httpServletRequest.getSession();

        if (httpSession.getAttribute(CHROMECAST_AUTH) == null && OUTSTANDING_AUTHS.containsKey(auth)) {
            httpSession.setAttribute(CHROMECAST_AUTH, auth);
        }
        if (httpSession.getAttribute(CHROMECAST_AUTH) == null) {
            throw new UnauthorizedException("You do not have a valid session. Please refresh and login again");
        }

        String targetUrl = "http://" + baseConfig.owncastInstance + httpServletRequest.getRequestURI().replace("/members/" + auth, "");
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
        OUTSTANDING_AUTHS.put(auth, System.currentTimeMillis());
        return ResponseEntity.ok(auth);
    }

    @Override
    public void runDailyTask() {
        long oneDay = TimeUnit.DAYS.toMillis(1);
        long now = System.currentTimeMillis();

        for (Object2LongMap.Entry<String> entry : OUTSTANDING_AUTHS.object2LongEntrySet()) {
            if (now - entry.getLongValue() >= oneDay) {
                OUTSTANDING_AUTHS.removeLong(entry.getKey());
            }
        }
    }

}