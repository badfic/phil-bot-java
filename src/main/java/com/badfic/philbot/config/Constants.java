package com.badfic.philbot.config;

import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
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
    String GIVEAWAY_CHANNEL = "giveaways";
    String MEGA_HELL_CHANNEL = "mega-hell";
    String MEGA_HELL_ROLE = "in mega hell";
    String TEST_CHANNEL = "test-channel";

    String DRY_BASTARDS_ROLE = "Dry Bastards";
    String DRY_CINNAMON_ROLE = "Dry Cinnamon Rolls";
    String SWAMPY_BASTARDS_ROLE = "Swampy Bastards";
    String SWAMPY_CINNAMON_ROLE = "Swampy Cinnamon Rolls";

    String DRY_CINNAMON_CHANNEL = "the-dry-cinnamon-rolls";
    String DRY_BASTARDS_CHANNEL = "the-dry-bastards";
    String SWAMPY_CINNAMON_CHANNEL = "the-swampy-cinnamon-rolls";
    String SWAMPY_BASTARD_CHANNEL = "the-swampy-bastards";

    String DRY_BASTARDS_CREST = "https://cdn.discordapp.com/attachments/741053845098201099/782478360491196416/dry_bastard.png";
    String DRY_CINNAMON_CREST = "https://cdn.discordapp.com/attachments/741053845098201099/782478362391478322/dry_cinnamon_roll.png";
    String SWAMPY_BASTARDS_CREST = "https://cdn.discordapp.com/attachments/741053845098201099/782478365411377193/swampy_bastard.png";
    String SWAMPY_CINNAMON_CREST = "https://cdn.discordapp.com/attachments/741053845098201099/782478367324635207/swampy_cinnamon_roll.png";

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

        footer = footer != null ? (footer + "\n" + pickRandom(FOOTERS)) : pickRandom(FOOTERS);
        footer = footer.length() > 2048 ? (footer.substring(0, 2044) + "777") : footer;

        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(finalDesc)
                .setImage(image)
                .setColor(color)
                .setFooter(footer)
                .setThumbnail(thumbnail)
                .build();
    }

    Set<String> FOOTERS = ImmutableSet.of(
            "powered by 777 shreks",
            "powered by 777 streggs",
            "powered by 777 deans",
            "powered by 777 castiels",
            "powered by 777 sams",
            "powered by 777 billys",
            "powered by 777 toms",
            "powered by 777 steves",
            "powered by 777 bettys",
            "powered by 777 jugheads",
            "powered by 777 riverdale memes",
            "powered by 777 aprils",
            "powered by 777 shrimps",
            "powered by 777 z0mbs",
            "powered by 777 memes",
            "powered by 777 grapefruit",
            "powered by 777 pineapples",
            "powered by 777 grapes",
            "powered by 777 marshmallows",
            "powered by 777 beers",
            "powered by 777 coffees",
            "powered by 777 sticks of butter",
            "powered by 777 pounds of butter",
            "powered by 777 receipts",
            "powered by 777 swamps",
            "powered by 777 gallons of swamp water",
            "powered by 777 gallons of milk",
            "powered by 777 gallons of glogg",
            "powered by 777 slices of pizza",
            "powered by 777 youtubers",
            "powered by 777 tiktokers",
            "powered by 777 influencers",
            "powered by 777 mall santas",
            "powered by 777 vaccines",
            "powered by 777 anti-vaxers",
            "powered by 777 politicians",
            "powered by 777 cinnamon rolls",
            "powered by 777 garys",
            "powered by 777 bettys",
            "powered by 777 aprils",
            "powered by 777 2020s",
            "powered by 777 stevies",
            "powered by 777 wallys",
            "powered by 777 karens",
            "powered by 777 monopoly games",
            "powered by 777 chicken nuggets",
            "powered by 777 rays",
            "powered by 777 donnas",
            "powered by 777 zaris",
            "powered by 777 darhks",
            "powered by 777 days",
            "powered by 777 nights",
            "powered by 777 summers",
            "powered by 777 winters",
            "powered by 777 falls",
            "powered by 777 autumns",
            "powered by 777 springs",
            "powered by 777 april showers",
            "powered by 777 may flowers",
            "powered by 777 rainy seasons",
            "powered by 777 droughts",
            "powered by 777 WHY THE FUCK IS IT DARK AT 4PMs",
            "powered by 777 buckets",
            "powered by 777 glo ups",
            "powered by 777",
            "powered by 777 tires",
            "powered by 777 Beckys with the good hair",
            "powered by 777 cards",
            "powered by 777 sandwiches",
            "powered by 777 shenanigans",
            "powered by 777 catfish",
            "powered by 777 fish",
            "powered by 777 salmon",
            "powered by 777 dogs",
            "powered by 777 cats",
            "powered by 777 hamsters",
            "powered by 777 mumble rappers",
            "powered by 777 grinches",
            "powered by 777 matthew morrisons",
            "powered by 777 phil klemmers",
            "powered by 777 ayys",
            "powered by 777 lmaos",
            "powered by 777 constantines",
            "powered by 777 unicorns",
            "powered by 777 uncle guggies",
            "powered by 777 pepsis",
            "powered by 777 cokes",
            "powered by 777 benjis",
            "powered by 777 fires",
            "powered by 777 buffalo",
            "powered by 777 taps",
            "powered by 777 hissy fits",
            "powered by 777 electricity",
            "powered by 777 daddys",
            "powered by 777 \uD83D\uDC40",
            "powered by 777 eggs",
            "powered by 777 buffets",
            "powered by 777 maps",
            "powered by 777 tumblrs",
            "powered by 777 tweets",
            "powered by 777 twitters",
            "powered by 777 videos",
            "powered by 777 gifs",
            "powered by 777 shrimp",
            "powered by 777 shremp"
    );

}
