package com.badfic.philbot.commands;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Synchronized;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Getter
@EqualsAndHashCode(of = "ordinal")
public class Rank {
    public static final long LVL_MULTIPLIER = 2000;
    private static final Map<Long, Rank> LEVEL_MAP = new HashMap<>();

    private final int ordinal;
    private final String roleName;
    private final long level;
    private final String rankUpImage;
    private final String rankUpMessage;
    private final Color color;
    private final boolean newCardDeck;
    private final boolean firstRank;

    private Rank(int ordinal, String roleName, long level, String rankUpImage, String rankupMessage, Color color) {
        this.ordinal = ordinal;
        this.roleName = roleName;
        this.level = level;
        this.rankUpImage = rankUpImage;
        this.rankUpMessage = rankupMessage;
        this.color = color;
        this.newCardDeck = ordinal % 3 == 0;
        this.firstRank = ordinal == 0;
    }

    @Synchronized
    public static void init(RestTemplate restTemplate, String airtableApiToken) throws Exception {
        LEVEL_MAP.clear();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + airtableApiToken);
        ResponseEntity<RecordList> response = restTemplate.exchange("https://api.airtable.com/v0/appYjP1F2Li4DAR1m/tblDmx7RE0kEP0p48",
                HttpMethod.GET, new HttpEntity<>(httpHeaders), RecordList.class);

        RecordList recordList = response.getBody();

        List<TableRecord> records = recordList.records();

        records.sort(Comparator.comparingLong(r -> r.fields().level()));

        for (int i = 0; i < records.size(); i++) {
            TableRecord record = records.get(i);
            RecordFields fields = record.fields();

            Rank rank = new Rank(i, fields.role(), fields.level(), fields.image(), fields.blurb(), Color.decode(fields.colour()));
            if (LEVEL_MAP.get(fields.level()) != null) {
                throw new IllegalStateException("Check swampy levels airtable, there's a duplicate level: " + fields.level());
            }

            LEVEL_MAP.put(fields.level(), rank);
        }
    }

    @Synchronized
    public static Rank byXp(long xp) {
        long level = xp / LVL_MULTIPLIER;

        for (long i = level; i > 0; i--) {
            if (LEVEL_MAP.containsKey(i)) {
                return LEVEL_MAP.get(i);
            }
        }

        return LEVEL_MAP.get(0L);
    }

    @Synchronized
    public static List<Rank> getAllRanks() {
        return LEVEL_MAP.values().stream().sorted(Comparator.comparingInt(Rank::getOrdinal)).collect(Collectors.toList());
    }

    public record RecordFields(String role, long level, String colour, String blurb, String image) {}
    public record TableRecord(RecordFields fields) {}
    public record RecordList(List<TableRecord> records) {}

}
