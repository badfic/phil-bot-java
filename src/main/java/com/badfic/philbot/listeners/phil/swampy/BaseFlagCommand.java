package com.badfic.philbot.listeners.phil.swampy;

import com.google.common.collect.ImmutableMap;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.util.ResourceUtils;

public abstract class BaseFlagCommand extends BaseSwampy {
    public static final String[] FLAG_NAMES = new String[] {
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
                BufferedImage image = ImageIO.read(ResourceUtils.getFile("classpath:flags/" + flagName + ".png"));
                builder.put(flagName, image);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        PRIDE_IMAGES = builder.build();
    }

}
