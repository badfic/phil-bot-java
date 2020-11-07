package com.badfic.philbot.web;

import java.lang.invoke.MethodHandles;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @GetMapping(value = "/logout", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> logout(HttpSession httpSession) {
        try {
            httpSession.invalidate();

            return ResponseEntity.ok("You have successfully logged out");
        } catch (Exception e) {
            logger.error("Exception in get trivia form controller", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("You are either unauthorized or something broke, ask Santiago");
        }
    }

}
