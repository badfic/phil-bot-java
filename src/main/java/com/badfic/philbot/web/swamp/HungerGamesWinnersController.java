package com.badfic.philbot.web.swamp;

import com.badfic.philbot.service.HungerGamesWinnersService;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HungerGamesWinnersController {

    @Resource
    private HungerGamesWinnersService hungerGamesWinnersService;

    @CrossOrigin
    @GetMapping(value = "/hunger-games-winners", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> get() {
        return ResponseEntity.ok(hungerGamesWinnersService.getMessages());
    }

}
