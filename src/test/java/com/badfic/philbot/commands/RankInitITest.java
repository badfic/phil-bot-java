package com.badfic.philbot.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class RankInitITest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    public void testRankInit() throws Exception {
        Rank.RecordList recordList;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("airtable-response.json")) {
            recordList = new ObjectMapper().readValue(stream, Rank.RecordList.class);
        }

        Mockito.doReturn(new ResponseEntity<>(recordList, HttpStatusCode.valueOf(200)))
                .when(restTemplate)
                .exchange(Mockito.eq("https://api.airtable.com/v0/appYjP1F2Li4DAR1m/tblDmx7RE0kEP0p48"), Mockito.eq(HttpMethod.GET),
                        Mockito.any(HttpEntity.class), Mockito.eq(Rank.RecordList.class));

        Rank.init(restTemplate, "fake-api-key");
    }
}
