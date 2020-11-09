package com.badfic.philbot.web;

import com.badfic.philbot.config.NewSessionException;
import javax.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController extends BaseController {

    @GetMapping(value = "/logout", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> logout(HttpSession httpSession) {
        if (httpSession.isNew()) {
            throw new NewSessionException();
        }

        httpSession.invalidate();
        return ResponseEntity.ok("You have successfully logged out");
    }

}
