package com.badfic.philbot.commands;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class RankInitITest {
    @Test
    public void testRankInit() throws Exception {
        Rank.init(new RestTemplate(), System.getenv("AIRTABLE_API_TOKEN"));
    }
}
