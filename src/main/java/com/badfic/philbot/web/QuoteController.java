package com.badfic.philbot.web;

import com.badfic.philbot.data.phil.QuoteRepository;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuoteController extends BaseController {

    @Resource
    private MustacheFactory mustacheFactory;

    @Resource
    private QuoteRepository quoteRepository;

    private Mustache mustache;

    @PostConstruct
    public void init() {
        mustache = mustacheFactory.compile("quotes.mustache");
    }

    @GetMapping(value = "/quotes", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getRanks(HttpSession httpSession) throws Exception {
        checkSession(httpSession);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Quotes");
        props.put("quotes", quoteRepository.findAll());

        try (ReusableStringWriter stringWriter = ReusableStringWriter.getCurrent()) {
            mustache.execute(stringWriter, props);
            return ResponseEntity.ok(stringWriter.toString());
        }
    }

}
