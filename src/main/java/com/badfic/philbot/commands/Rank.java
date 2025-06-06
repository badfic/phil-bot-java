package com.badfic.philbot.commands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Synchronized;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

@Getter
@EqualsAndHashCode(of = "ordinal")
public class Rank {
    // Formula for xp to reach next level: XP = (level/0.07)^2
    // Formula for current level: level = 0.07 * √XP

    private static final double LEVEL_MODIFIER = 0.07D;
    private static final Long2ObjectMap<Rank> LEVEL_MAP = new Long2ObjectOpenHashMap<>(100);

    private final int ordinal;
    private final String roleName;
    private final long level;
    private final String rankUpImage;
    private final String rankUpMessage;
    private final Color color;
    private final boolean newCardDeck;
    private final boolean firstRank;

    private Rank(final int ordinal, final String roleName, final long level, final String rankUpImage, final String rankupMessage, final Color color) {
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
    public static void init(final RestTemplate restTemplate, final String airtableApiToken) {
        LEVEL_MAP.clear();

        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + airtableApiToken);
        final var response = restTemplate.exchange("https://api.airtable.com/v0/appYjP1F2Li4DAR1m/tblqH0ym9Dcza7oDG",
                HttpMethod.GET, new HttpEntity<>(httpHeaders), RecordList.class);

        final var recordList = response.getBody();

        final var records = recordList.records();

        records.sort(Comparator.comparingLong(r -> r.fields().level()));

        for (var i = 0; i < records.size(); i++) {
            final var record = records.get(i);
            final var fields = record.fields();

            final var rank = new Rank(i, fields.role(), fields.level(), fields.image(), fields.blurb(), Color.decode(fields.colour()));
            if (LEVEL_MAP.get(fields.level()) != null) {
                throw new IllegalStateException("Check swampy levels airtable, there's a duplicate level: " + fields.level());
            }

            LEVEL_MAP.put(fields.level(), rank);
        }
    }

    @Synchronized
    public static Rank byXp(final long xp) {
        final var level = Math.round(LEVEL_MODIFIER * Math.sqrt(xp));

        for (var i = level; i > 0; i--) {
            if (LEVEL_MAP.containsKey(i)) {
                return LEVEL_MAP.get(i);
            }
        }

        return LEVEL_MAP.get(0L);
    }

    public static long xpRequiredForLevel(final long xp, final long level) {
        return Math.round(Math.pow(level/ LEVEL_MODIFIER, 2) - xp);
    }

    @Synchronized
    public static List<Rank> getAllRanks() {
        return LEVEL_MAP.values().stream().sorted(Comparator.comparingInt(Rank::getOrdinal)).toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RecordFields(String role, long level, String colour, String blurb, String image) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TableRecord(RecordFields fields) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RecordList(List<TableRecord> records) {}

}
