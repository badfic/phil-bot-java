package com.badfic.philbot.web;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.web.members.BaseMembersController;
import com.badfic.philbot.web.members.HomeController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {
    private final BaseConfig baseConfig;

    public GlobalControllerExceptionHandler(BaseConfig baseConfig) {
        this.baseConfig = baseConfig;
    }

    @ExceptionHandler(BaseMembersController.NewSessionException.class)
    public ResponseEntity<Object> handleNewSessionException(BaseMembersController.NewSessionException e) {
        StringBuilder urlBuilder = new StringBuilder()
                .append("https://discord.com/api/oauth2/authorize?client_id=")
                .append(baseConfig.discordClientId)
                .append("&redirect_uri=")
                .append(URLEncoder.encode(baseConfig.hostname + "/members/home", StandardCharsets.UTF_8))
                .append("&response_type=code&scope=identify");
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, urlBuilder.toString()).build();
    }

    @ExceptionHandler(HomeController.LoginRedirectException.class)
    public ResponseEntity<Object> handleLoginRedirectException(HomeController.LoginRedirectException e) {
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, baseConfig.hostname + e.getUrl()).build();
    }

    @ExceptionHandler(BaseMembersController.UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(BaseMembersController.UnauthorizedException e, HttpSession httpSession) {
        log.error("Unauthorized Exception caught at top level", e);
        httpSession.invalidate();
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e, HttpServletRequest request) {
        log.error("Controller Exception caught at top level", e);
        return new ResponseEntity<>("Generic Top Level Exception, ask Santiago. Something might have broke.", HttpStatus.UNAUTHORIZED);
    }

}
