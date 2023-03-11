package com.badfic.philbot.service;

import com.badfic.philbot.data.phil.Rank;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class RankInitITest {
    @Test
    public void testRankInit() throws Exception {
        Rank.init(new RestTemplate(), System.getenv("AIRTABLE_API_TOKEN"));
    }
}
