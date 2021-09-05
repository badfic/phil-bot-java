package com.badfic.philbot.web;

import com.badfic.philbot.service.DailyMarvelMemeService;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DailyMarvelMemeController extends BaseController {

    @Resource
    private DailyMarvelMemeService dailyMarvelMemeService;

    @CrossOrigin
    @GetMapping(value = "/daily-marvel-memes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> get() {
        return ResponseEntity.ok(dailyMarvelMemeService.getMessages());
    }

}
