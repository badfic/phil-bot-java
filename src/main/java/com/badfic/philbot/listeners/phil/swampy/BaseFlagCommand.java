package com.badfic.philbot.listeners.phil.swampy;

import com.google.common.collect.ImmutableMap;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;

public abstract class BaseFlagCommand extends BaseSwampy {
    protected static final String[] FLAG_NAMES = new String[] {
            "lesbian",
            "gay",
            "bi",
            "questioning",
            "pan",
            "ace",
            "aro",
            "aroace",
            "demi",
            "trans",
            "enby",
            "intersex",
            "auto",
            "trixic",
            "genderfluid",
            "vincian",
            "sapphic",
            "achillean",
            "poly"
    };
    protected static final Map<String, BufferedImage> PRIDE_IMAGES;

    static {
        ImmutableMap.Builder<String, BufferedImage> builder = ImmutableMap.builder();

        for (String flagName : FLAG_NAMES) {
            try {
                InputStream prideFlagStream = BaseFlagCommand.class.getClassLoader().getResourceAsStream("flags/" + flagName + ".png");
                BufferedImage image = ImageIO.read(Objects.requireNonNull(prideFlagStream));
                builder.put(flagName, image);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        PRIDE_IMAGES = builder.build();
    }

}
