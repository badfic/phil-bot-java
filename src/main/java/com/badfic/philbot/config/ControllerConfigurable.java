package com.badfic.philbot.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface ControllerConfigurable {
    enum Type {
        INT, LONG, STRING, STRING_SET, IMG
    }

    enum Category {
        MESSAGE, UPVOTE, SLOTS, CARROT, POTATO, GLITTER, BOOST, REFUND, SWEEPSTAKES, TAXES, THIS_OR_THAT, BOTS, COLORS_FOOTERS, TRIVIAS, SCOOT, STONK_SHREK,
        SWIPER, CHANNEL_IDS
    }

    Type type();

    Category category();

}
