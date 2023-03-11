package com.badfic.philbot.service;

import com.badfic.philbot.data.phil.Rank;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class RankInitTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    public void testRankInitialization() throws Exception {
        List<Rank.TableRecord> tableRecords = new ArrayList<>();

        tableRecords.add(new Rank.TableRecord(new Rank.RecordFields(
                "Cinnamon Role", 0, "#000000", "you're a cinnamon roll", "http://example.com")));
        tableRecords.add(new Rank.TableRecord(new Rank.RecordFields(
                "Second Role", 1, "#ffffff", "you're a different roll", "http://example.com")));

        Mockito.doReturn(new ResponseEntity<>(new Rank.RecordList(tableRecords), HttpStatus.OK))
                .when(restTemplate)
                .exchange(Mockito.eq("https://api.airtable.com/v0/appYjP1F2Li4DAR1m/tblDmx7RE0kEP0p48"), Mockito.eq(HttpMethod.GET),
                        Mockito.any(HttpEntity.class), Mockito.eq(Rank.RecordList.class));

        Rank.init(restTemplate, UUID.randomUUID().toString());

        Assertions.assertEquals(2, Rank.getAllRanks().size());
    }

}
