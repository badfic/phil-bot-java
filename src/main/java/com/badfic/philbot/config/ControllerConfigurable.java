package com.badfic.philbot.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface ControllerConfigurable {
    enum Type {
        INT, LONG, STRING, IMG
    }

    Type type();

}
