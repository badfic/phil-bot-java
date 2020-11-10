package com.badfic.philbot.web;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.config.NewSessionException;
import com.badfic.philbot.config.UnauthorizedException;
import io.honeybadger.reporter.HoneybadgerReporter;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Resource
    private BaseConfig baseConfig;

    @Resource
    private HoneybadgerReporter honeybadgerReporter;

    @ExceptionHandler(NewSessionException.class)
    public ResponseEntity<Object> handleNewSessionException(NewSessionException e) throws Exception {
        StringBuilder urlBuilder = new StringBuilder()
                .append("https://discord.com/api/oauth2/authorize?client_id=")
                .append(baseConfig.discordClientId)
                .append("&redirect_uri=")
                .append(URLEncoder.encode(baseConfig.hostname, StandardCharsets.UTF_8.name()))
                .append("&response_type=code&scope=identify");
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, urlBuilder.toString()).build();
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException e, HttpSession httpSession) {
        logger.error("Unauthorized Exception caught at top level", e);
        httpSession.invalidate();
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e, HttpServletRequest request) {
        logger.error("Exception caught at top level", e);
        honeybadgerReporter.reportError(e, request, "Controller Exception caught at top level");
        return new ResponseEntity<>("Generic Top Level Exception, ask Santiago. Something might have broke.", HttpStatus.UNAUTHORIZED);
    }

}
