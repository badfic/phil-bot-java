package com.badfic.philbot.config;

import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

public interface Constants {
    String ADMIN_ROLE = "Queens of the Castle";
    String MOD_ROLE = "Princess of the Castle";
    String EIGHTEEN_PLUS_ROLE = "18+";
    String CHAOS_CHILDREN_ROLE = "Chaos Children";
    String SWAMPYS_CHANNEL = "the-swampys";
    String TEST_CHANNEL = "test-channel";

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
}
