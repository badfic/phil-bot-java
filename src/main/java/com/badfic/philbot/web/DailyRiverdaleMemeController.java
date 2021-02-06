package com.badfic.philbot.web;

import com.badfic.philbot.service.DailyRiverdaleMemeService;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DailyRiverdaleMemeController extends BaseController {

    @Resource
    private DailyRiverdaleMemeService dailyRiverdaleMemeService;

    @CrossOrigin
    @GetMapping(value = "/daily-riverdale-memes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> get() {
        return ResponseEntity.ok(dailyRiverdaleMemeService.getMessages());
    }

}
