package com.badfic.philbot.web.members;

import com.badfic.philbot.data.Trivia;
import com.badfic.philbot.data.TriviaRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class TriviaController extends BaseMembersController {

    private final TriviaRepository triviaRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    @GetMapping(value = "/trivia", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getTriviaForm(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        final var triviaList = triviaRepository.findAll().stream().map(t ->
            new TriviaForm(t.getId(), t.getQuestion(), t.getAnswerA(), t.getAnswerB(), t.getAnswerC(), TriviaForm.TriviaAnswer.values()[t.getCorrectAnswer()])
        ).toList();

        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Submit A New Trivia Question");
        addCommonProps(httpServletRequest.getSession(), props);
        props.put("trivia", triviaList);

        return new ModelAndView("trivia", props);
    }

    @PostMapping(value = "/trivia", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> postTriviaForm(@RequestBody final TriviaForm form, final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        if (Stream.of(form.question(), form.answerA(), form.answerB(), form.answerC()).anyMatch(StringUtils::isBlank)) {
            return ResponseEntity.badRequest().body("Please provide a valid question, A, B, C, and answer");
        }
        if (form.correctAnswer() == null) {
            return ResponseEntity.badRequest().body("Please provide a valid question, A, B, C, and answer");
        }

        final var trivia = new Trivia();
        trivia.setId(UUID.randomUUID());
        trivia.setQuestion(form.question());
        trivia.setAnswerA(form.answerA());
        trivia.setAnswerB(form.answerB());
        trivia.setAnswerC(form.answerC());
        trivia.setCorrectAnswer((short) form.correctAnswer().ordinal());

        jdbcAggregateTemplate.insert(trivia);

        return ResponseEntity.ok("Successfully created new trivia question! Thank you for your submission! Refresh to see it below.");
    }

    public record TriviaForm(UUID uuid, String question, String answerA, String answerB, String answerC, TriviaAnswer correctAnswer) {
        public enum TriviaAnswer {
            A, B, C
        }
    }

}
