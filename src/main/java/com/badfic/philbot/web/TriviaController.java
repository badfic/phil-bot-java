package com.badfic.philbot.web;

import com.badfic.philbot.data.TriviaForm;
import com.badfic.philbot.data.phil.Trivia;
import com.badfic.philbot.data.phil.TriviaRepository;
import com.google.common.io.CharStreams;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TriviaController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Resource
    private TriviaRepository triviaRepository;

    private String triviaHtml;

    @PostConstruct
    public void init() {
        try {
            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("static/trivia-form.html");
            triviaHtml = CharStreams.toString(new InputStreamReader(Objects.requireNonNull(resourceAsStream), StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Failed to read trivia-form.html", e);
            triviaHtml = "Contact Santiago, it broke";
        }
    }

    @GetMapping(value = "/trivia", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getTriviaForm(HttpSession httpSession) {
        try {
            if (httpSession.isNew()) {
                return redirectDiscordLogin();
            }
            if (httpSession.getAttribute(DISCORD_TOKEN) == null) {
                httpSession.invalidate();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You do not have a valid session. Please refresh and try again");
            }
            refreshTokenIfNeeded(httpSession);

            return ResponseEntity.ok(triviaHtml);
        } catch (Exception e) {
            logger.error("Exception in get trivia form controller", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("You are either unauthorized or something broke, ask Santiago");
        }
    }

    @PostMapping(value = "/trivia", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> postTriviaForm(@RequestBody TriviaForm form, HttpSession httpSession) {
        try {
            if (httpSession.isNew()) {
                return redirectDiscordLogin();
            }
            if (httpSession.getAttribute(DISCORD_TOKEN) == null) {
                httpSession.invalidate();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You do not have a valid session. Please refresh and try again");
            }
            refreshTokenIfNeeded(httpSession);

            if (Stream.of(form.getQuestion(), form.getAnswerA(), form.getAnswerB(), form.getAnswerC()).anyMatch(StringUtils::isBlank)) {
                return ResponseEntity.badRequest().body("Please provide a valid question, A, B, C, and answer");
            }
            if (form.getCorrectAnswer() == null) {
                return ResponseEntity.badRequest().body("Please provide a valid question, A, B, C, and answer");
            }

            Trivia trivia = new Trivia();
            trivia.setId(UUID.randomUUID());
            trivia.setQuestion(form.getQuestion());
            trivia.setAnswerA(form.getAnswerA());
            trivia.setAnswerB(form.getAnswerB());
            trivia.setAnswerC(form.getAnswerC());
            trivia.setCorrectAnswer((short) form.getCorrectAnswer().ordinal());

            triviaRepository.save(trivia);

            return ResponseEntity.ok("Successfully created new trivia question! Thank you for your submission!");
        } catch (Exception e) {
            logger.error("Exception in post trivia form controller", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something broke, bug Santiago to check the logs");
        }
    }

}
