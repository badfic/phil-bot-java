package com.badfic.philbot.web.ao3;

import com.badfic.philbot.config.BaseConfig;
import com.badfic.philbot.service.Ao3MetadataParser;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Ao3SummaryController {

    @Setter(onMethod_ = {@Autowired})
    protected BaseConfig baseConfig;

    @Setter(onMethod_ = {@Autowired})
    protected Ao3MetadataParser ao3MetadataParser;

    @PostMapping(value = "/ao3/summary", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getAo3Summary(@RequestBody Ao3SummaryRequest ao3SummaryRequest) {
        if (!baseConfig.ao3SummaryApiKey.equals(ao3SummaryRequest.apiKey())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ao3MetadataParser.sendSummaryToWebhook(ao3SummaryRequest.ao3Url(), ao3SummaryRequest.webhookUrl());
        return ResponseEntity.ok("ok");
    }

    public record Ao3SummaryRequest(String apiKey, String ao3Url, String webhookUrl) {}
}
