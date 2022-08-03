package com.badfic.philbot.data.phil;

import com.badfic.philbot.config.Constants;
import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class Rank {
    public static final long LVL_MULTIPLIER = 2000;
    private static final Map<Long, Rank> LEVEL_MAP = new HashMap<>();
    private static final Set<String> LEVEL_ROLE_NAMES = new HashSet<>();
    private static final String TEMPLATE_NAME = "GLOBAL_LEVEL_TEMPLATE";

    private final int ordinal;
    private final String roleName;
    private final long level;
    private final String rankUpImage;
    private final String rankUpMessage;
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean newCardDeck;

    private Rank(int ordinal, String roleName, long level, String rankUpImage, String rankupMessage) {
        this.ordinal = ordinal;
        this.roleName = roleName;
        this.level = level;
        this.rankUpImage = rankUpImage;
        this.rankUpMessage = rankupMessage;
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

            Rank rank = new Rank(i - 1, roleName, level, rankUpImage, rankUpMessage);

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

    @Deprecated
    private static CompletableFuture<?> performReset(JDA jda) {
        // LONG STORY SHORT: Discord rate limits role creation to something along the lines of 200 per day. And if you make more than 200 roles in a day
        // you are then rate-limited for up to 48 hours! So we can't run this, even though it would make our lives way easier not having to create
        // roles manually every month. Oh well.
        Role templateRole = jda.getRolesByName(TEMPLATE_NAME, true).get(0);

        List<String> lines;
        try {
            lines = IOUtils.readLines(Objects.requireNonNull(Rank.class.getClassLoader().getResourceAsStream("rank.tsv")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Constants.debugToTestChannel(jda, "Failed to load and create levels!!!");
            return CompletableFuture.completedFuture(null);
        }

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (int i = lines.size() - 1; i >= 1; i--) {
            String line = lines.get(i);

            String[] values = StringUtils.split(line, '\t');

            String roleName = StringUtils.strip(values[0]);
            String color = StringUtils.strip(values[4]);

            List<Role> rolesByName = jda.getRolesByName(roleName, false);

            RoleAction createRoleAction = jda.getGuilds().get(0)
                    .createCopyOfRole(templateRole)
                    .setName(roleName)
                    .setColor(hex2Rgb(color));

            if (CollectionUtils.isNotEmpty(rolesByName)) {
                futures.add(rolesByName.get(0)
                        .delete()
                        .submit()
                        .thenRun(createRoleAction::queue));
            } else {
                futures.add(createRoleAction.submit());
            }
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    // Accepts colors as #FFFFFF hex notation
    private static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.parseInt(colorStr.substring(1, 3), 16),
                Integer.parseInt(colorStr.substring(3, 5), 16),
                Integer.parseInt(colorStr.substring(5, 7), 16));
    }

}
