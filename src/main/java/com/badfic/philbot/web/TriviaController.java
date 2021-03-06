package com.badfic.philbot.web;

import com.badfic.philbot.data.TriviaForm;
import com.badfic.philbot.data.phil.Trivia;
import com.badfic.philbot.data.phil.TriviaRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class TriviaController extends BaseController {

    @Resource
    private TriviaRepository triviaRepository;

    @GetMapping(value = "/trivia", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getTriviaForm(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        List<TriviaForm> triviaList = triviaRepository.findAll().stream().map(t -> {
            TriviaForm triviaForm = new TriviaForm();
            triviaForm.setUuid(t.getId());
            triviaForm.setQuestion(t.getQuestion());
            triviaForm.setCorrectAnswer(TriviaForm.Answer.values()[t.getCorrectAnswer()]);
            triviaForm.setAnswerA(t.getAnswerA());
            triviaForm.setAnswerB(t.getAnswerB());
            triviaForm.setAnswerC(t.getAnswerC());
            return triviaForm;
        }).collect(Collectors.toList());

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Submit A New Trivia Question");
        props.put("username", httpServletRequest.getSession().getAttribute(DISCORD_USERNAME));
        props.put("isMod", httpServletRequest.getSession().getAttribute(DISCORD_IS_MOD));
        props.put("trivia", triviaList);

        return new ModelAndView("trivia", props);
    }

    @PostMapping(value = "/trivia", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> postTriviaForm(@RequestBody TriviaForm form, HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

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

        return ResponseEntity.ok("Successfully created new trivia question! Thank you for your submission! Refresh to see it below.");
    }

}
