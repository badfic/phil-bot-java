package com.badfic.philbot.web;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HlsProxyController extends BaseController {

    @GetMapping(value = "/hls/**")
    public ResponseEntity<byte[]> hlsProxy(HttpServletRequest httpServletRequest) {
//        HttpSession httpSession = httpServletRequest.getSession();
//        if (httpSession.isNew()) {
//            throw new NewSessionException();
//        }
//        if (httpSession.getAttribute(DISCORD_TOKEN) == null) {
//            throw new UnauthorizedException("You do not have a valid session. Please refresh and login again");
//        }

        return restTemplate.getForEntity("http://" + baseConfig.owncastInstance + httpServletRequest.getRequestURI(), byte[].class);
    }

}