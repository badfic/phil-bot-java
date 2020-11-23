package com.badfic.philbot.config;

import java.awt.Color;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

public interface Constants {
    String ADMIN_ROLE = "Queens of the Castle";
    String MOD_ROLE = "Princess of the Castle";
    String EIGHTEEN_PLUS_ROLE = "18+";
    String CHAOS_CHILDREN_ROLE = "Chaos Children";
    String SWAMPYS_CHANNEL = "the-swampys";
    String MEGA_HELL_CHANNEL = "mega-hell";
    String MEGA_HELL_ROLE = "in mega hell";
    String TEST_CHANNEL = "test-channel";

    String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36";

    Color COLOR_OF_THE_MONTH = new Color(225, 168, 0);
    Color SWAMP_GREEN = new Color(89, 145, 17);

    static <T> T pickRandom(Collection<T> collection) {
        int index = ThreadLocalRandom.current().nextInt(collection.size());
        Iterator<T> iterator = collection.iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    static String prettyPrintDuration(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

}
