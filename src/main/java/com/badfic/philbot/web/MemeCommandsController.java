package com.badfic.philbot.web;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.phil.MemeCommandEntity;
import com.badfic.philbot.data.phil.MemeCommandRepository;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class MemeCommandsController extends BaseController {

    @Resource
    private MemeCommandRepository memeCommandRepository;

    @GetMapping(value = "/meme-commands", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        List<MemeCommandEntity> memes = memeCommandRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        memes.forEach(meme -> meme.setUrlIsImage(Constants.urlIsImage(meme.getUrl())));

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Meme Commands");
        props.put("username", httpServletRequest.getSession().getAttribute(DISCORD_USERNAME));
        props.put("isMod", httpServletRequest.getSession().getAttribute(DISCORD_IS_MOD));
        props.put("commands", memes);

        return new ModelAndView("meme-commands", props);
    }

    @PostMapping(value = "/meme-commands", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> postMemeForm(@RequestBody MemeForm form, HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        if (Stream.of(form.getMemeName(), form.getMemeUrl()).anyMatch(StringUtils::isBlank)) {
            return ResponseEntity.badRequest().body("Please provide a valid name and url");
        }

        String lowercaseName = StringUtils.lowerCase(form.getMemeName());

        if (!StringUtils.isAlphanumeric(lowercaseName)) {
            return ResponseEntity.badRequest().body("A meme name can only be alphanumeric. No symbols.");
        }

        try {
            URL u = new URL(form.getMemeUrl());
            Objects.requireNonNull(u.toURI());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("A meme url must be a valid url.");
        }

        if (memeCommandRepository.findById(lowercaseName).isPresent()) {
            return ResponseEntity.badRequest().body("A meme by that name already exists");
        }

        MemeCommandEntity meme = new MemeCommandEntity();
        meme.setName(lowercaseName);
        meme.setUrl(form.getMemeUrl());

        memeCommandRepository.save(meme);

        return ResponseEntity.ok("Successfully created new meme command! Refresh the page to see it below.");
    }

    @DeleteMapping(value = "/meme-commands/{memeName}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> deleteMeme(@PathVariable("memeName") String memeName, HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        if (StringUtils.isBlank(memeName)) {
            return ResponseEntity.badRequest().body("memeName provided was blank");
        }

        if (!StringUtils.isAlphanumeric(memeName)) {
            return ResponseEntity.badRequest().body("memeName provided was not alphanumeric");
        }

        if (memeCommandRepository.findById(memeName).isPresent()) {
            memeCommandRepository.deleteById(memeName);
            return ResponseEntity.ok("Successfully deleted: " + memeName + ". Refresh the page to see it disappear");
        } else {
            return ResponseEntity.badRequest().body("memeName provided does not exist");
        }
    }

}
