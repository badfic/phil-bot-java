package com.badfic.philbot.web.members;

import com.badfic.philbot.data.phil.MemeCommandEntity;
import com.badfic.philbot.listeners.phil.MemeCommandsService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
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
public class MemeCommandsController extends BaseMembersController {

    @Autowired
    private MemeCommandsService memeCommandsService;

    @GetMapping(value = "/meme-commands", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        List<MemeCommandEntity> memes = memeCommandsService.findAll();

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Meme Commands");
        addCommonProps(httpServletRequest, props);
        props.put("commands", memes);

        return new ModelAndView("meme-commands", props);
    }

    @PostMapping(value = "/meme-commands", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> postMemeForm(@RequestBody MemeForm form, HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        memeCommandsService.saveMeme(form);

        return ResponseEntity.ok("Successfully created new meme command! Refresh the page to see it below.");
    }

    @DeleteMapping(value = "/meme-commands/{memeName}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> deleteMeme(@PathVariable("memeName") String memeName, HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        memeCommandsService.deleteMeme(memeName);
        return ResponseEntity.ok("Successfully deleted: " + memeName + ". Refresh the page to see it disappear");
    }

}
