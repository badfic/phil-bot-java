package com.badfic.philbot.data.phil;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class Rank {
    public static final long LVL_MULTIPLIER = 2000;
    private static final Map<Long, Rank> LEVEL_MAP = new HashMap<>();
    private static final Set<String> LEVEL_ROLE_NAMES = new HashSet<>();

    private final int ordinal;
    private final String roleName;
    private final long level;
    private final String rankUpImage;
    private final String rankUpMessage;
    private final Color color;
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean newCardDeck;

    private Rank(int ordinal, String roleName, long level, String rankUpImage, String rankupMessage, Color color) {
        this.ordinal = ordinal;
        this.roleName = roleName;
        this.level = level;
        this.rankUpImage = rankUpImage;
        this.rankUpMessage = rankupMessage;
        this.color = color;
        this.newCardDeck = ordinal % 3 == 0;
    }

    public int ordinal() {
        return ordinal;
    }

    public String getRoleName() {
        return roleName;
    }

    public long getLevel() {
        return level;
    }

    public String getRankUpImage() {
        return rankUpImage;
    }

    public String getRankUpMessage() {
        return rankUpMessage;
    }

    public Color getColor() {
        return color;
    }

    public boolean isNewCardDeck() {
        return ordinal % 3 == 0;
    }

    public boolean isFirstRank() {
        return ordinal == 0;
    }

    public static void init() throws Exception {
        List<String> lines = IOUtils.readLines(Objects.requireNonNull(Rank.class.getClassLoader().getResourceAsStream("rank.tsv")), StandardCharsets.UTF_8);

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] values = StringUtils.split(line, '\t');

            if (ArrayUtils.getLength(values) != 5) {
                throw new IllegalArgumentException("Rank spreadsheet malformed on line: " + i);
            }

            String roleName = StringUtils.strip(values[0]);
            long level = Long.parseLong(values[1]);
            String rankUpMessage = StringUtils.strip(values[2]);
            String rankUpImage = StringUtils.strip(values[3]);
            String color = StringUtils.strip(values[4]);

            Rank rank = new Rank(i - 1, roleName, level, rankUpImage, rankUpMessage, Color.decode(color));

            if (LEVEL_MAP.get(level) != null) {
                throw new IllegalStateException("Check rank.tsv, there's a duplicate level: " + level);
            }

            LEVEL_MAP.put(level, rank);
            LEVEL_ROLE_NAMES.add(roleName);
        }
    }

    public static Rank byXp(long xp) {
        long level = xp / LVL_MULTIPLIER;

        for (long i = level; i > 0; i--) {
            if (LEVEL_MAP.containsKey(i)) {
                return LEVEL_MAP.get(i);
            }
        }

        return LEVEL_MAP.get(0L);
    }

    public static Set<String> getAllRoleNames() {
        return LEVEL_ROLE_NAMES;
    }

    public static Rank[] getAllRanks() {
        return LEVEL_MAP.values().toArray(Rank[]::new);
    }

}
