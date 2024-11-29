package com.badfic.philbot.web.members;

import com.badfic.philbot.service.MemeCommandsService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class MemeCommandsController extends BaseMembersController {

    private final MemeCommandsService memeCommandsService;

    @GetMapping(value = "/meme-commands", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        final var memes = memeCommandsService.findAll();

        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Meme Commands");
        addCommonProps(httpServletRequest.getSession(), props);
        props.put("commands", memes);

        return new ModelAndView("meme-commands", props);
    }

    @PostMapping(value = "/meme-commands", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> postMemeForm(@RequestBody final MemeForm form, final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        memeCommandsService.saveMeme(form.memeUrl(), form.memeName());

        return ResponseEntity.ok("Successfully created new meme %s. Refresh the page to see it below.".formatted(form.memeName()));
    }

    @DeleteMapping(value = "/meme-commands/{memeName}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> deleteMeme(@PathVariable("memeName") final String memeName, final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        memeCommandsService.deleteMeme(memeName);
        return ResponseEntity.ok("Successfully deleted: %s. Refresh the page to see it disappear".formatted(memeName));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MemeForm(String memeName, String memeUrl) {}

}
