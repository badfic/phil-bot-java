package com.badfic.philbot.config;

import java.awt.Color;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;

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

    static MessageEmbed simpleEmbedThumbnail(String title, String description, String thumbnail) {
        return simpleEmbed(title, description, null, null, Constants.COLOR_OF_THE_MONTH, thumbnail);
    }

    static MessageEmbed simpleEmbed(String title, String description) {
        return simpleEmbed(title, description, null, null, Constants.COLOR_OF_THE_MONTH, null);
    }

    static MessageEmbed simpleEmbed(String title, String description, Color color) {
        return simpleEmbed(title, description, null, null, color, null);
    }

    static MessageEmbed simpleEmbed(String title, String description, String image) {
        return simpleEmbed(title, description, image, null, Constants.COLOR_OF_THE_MONTH, null);
    }

    static MessageEmbed simpleEmbed(String title, String description, String image, Color color) {
        return simpleEmbed(title, description, image, null, color, null);
    }

    static MessageEmbed simpleEmbed(String title, String description, String image, String footer) {
        return simpleEmbed(title, description, image, footer, Constants.COLOR_OF_THE_MONTH, null);
    }

    static MessageEmbed simpleEmbed(String title, String description, String image, String footer, Color color) {
        return simpleEmbed(title, description, image, footer, color, null);
    }

    static MessageEmbed simpleEmbed(String title, String description, String image, String footer, Color color, String thumbnail) {
        final String finalDesc;
        if (StringUtils.isNotBlank(description)) {
            finalDesc = description.length() > 2048 ? (description.substring(0, 2044) + "...") : description;
        } else {
            finalDesc = null;
        }

        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(finalDesc)
                .setImage(image)
                .setColor(color)
                .setFooter(footer)
                .setThumbnail(thumbnail)
                .build();
    }

}
