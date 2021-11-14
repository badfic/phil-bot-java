package com.badfic.philbot.web.members;

import com.badfic.philbot.config.NewSessionException;
import javax.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoutController extends BaseMembersController {

    @GetMapping(value = "/logout", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> logout(HttpSession httpSession) {
        if (httpSession.isNew()) {
            throw new NewSessionException();
        }

        httpSession.invalidate();
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, baseConfig.hostname).build();
    }

}
