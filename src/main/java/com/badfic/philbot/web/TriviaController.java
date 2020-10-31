package com.badfic.philbot.web;

import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.DiscordUserRepository;
import com.badfic.philbot.data.TriviaForm;
import com.badfic.philbot.data.phil.SwampyGamesConfigRepository;
import com.badfic.philbot.data.phil.Trivia;
import com.badfic.philbot.data.phil.TriviaRepository;
import com.google.common.io.CharStreams;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TriviaController {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Resource
    private DiscordUserRepository discordUserRepository;

    @Resource
    private SwampyGamesConfigRepository swampyGamesConfigRepository;

    @Resource
    private TriviaRepository triviaRepository;

    private String htmlForm;

    @PostConstruct
    public void init() {
        try {
            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("static/trivia-form.html");
            htmlForm = CharStreams.toString(new InputStreamReader(Objects.requireNonNull(resourceAsStream), StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Failed to read trivia-form.html", e);
            htmlForm = "Contact Santiago, it broke";
        }
    }

    @GetMapping(value = "/trivia/{triviaGuid}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getTriviaForm(@PathVariable("triviaGuid") UUID triviaGuid) {
        try {
            Optional<DiscordUser> optionalUser = discordUserRepository.findByTriviaGuid(triviaGuid);

            if (!optionalUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trivia Form Not Found, perhaps it expired. please re-trigger by DMing Phil again.");
            }

            DiscordUser user = optionalUser.get();

            if (user.getTriviaGuidTime().plus(15, ChronoUnit.MINUTES).isBefore(LocalDateTime.now())) {
                user.setTriviaGuid(null);
                user.setTriviaGuidTime(null);
                discordUserRepository.save(user);
                return ResponseEntity.badRequest().body("Trivia form expired, please re-trigger by DMing Phil again");
            }

            return ResponseEntity.ok(htmlForm);
        } catch (Exception e) {
            logger.error("Exception in get trivia form controller", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something broke, bug Santiago");
        }
    }

    @CrossOrigin
    @PostMapping(value = "/trivia/{triviaGuid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> postTriviaForm(@PathVariable("triviaGuid") UUID triviaGuid, @RequestBody TriviaForm form) {
        try {
            Optional<DiscordUser> optionalUser = discordUserRepository.findByTriviaGuid(triviaGuid);

            if (!optionalUser.isPresent()) {
                return ResponseEntity.badRequest().body("Trivia form expired, please re-trigger by DMing phil again");
            }

            if (Stream.of(form.getQuestion(), form.getAnswerA(), form.getAnswerB(), form.getAnswerC()).anyMatch(StringUtils::isBlank)) {
                return ResponseEntity.badRequest().body("Please provide a valid question, A, B, C, and answer");
            }
            if (form.getCorrectAnswer() == null) {
                return ResponseEntity.badRequest().body("Please provide a valid question, A, B, C, and answer");
            }

            form.setUuid(triviaGuid);

            DiscordUser user = optionalUser.get();
            user.setTriviaGuid(null);
            user.setTriviaGuidTime(null);
            discordUserRepository.save(user);

            Trivia trivia = new Trivia();
            trivia.setId(triviaGuid);
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
