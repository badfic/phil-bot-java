package com.badfic.philbot.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface ControllerConfigurable {
    enum Type {
        INT, LONG, STRING, STRING_SET, IMG
    }

    enum Category {
        MESSAGE, UPVOTE, SLOTS, BOOST, THIS_OR_THAT, BOTS, COLORS_FOOTERS, TRIVIAS, CHANNEL_IDS
    }

    Type type();

    Category category();

}
