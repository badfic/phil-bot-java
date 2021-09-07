package com.badfic.philbot.config;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.awt.Color;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Constants {
    Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    String PREFIX = "!!";

    String ADMIN_ROLE = "Queens of the Castle";
    String EIGHTEEN_PLUS_ROLE = "18+";
    String CHAOS_CHILDREN_ROLE = "Chaos Children";
    String SWAMPYS_CHANNEL = "the-swampys";
    String MEGA_HELL_CHANNEL = "mega-hell";
    String MEGA_HELL_ROLE = "in mega hell";
    String TEST_CHANNEL = "test-channel";
    String HUNGERDOME_CHANNEL = "the-hungerdome";
    String CURSED_SWAMP_CHANNEL = "cursed-swamp";
    String MEMES_CHANNEL = "memes";

    String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36";

    Color SWAMP_GREEN = new Color(89, 145, 17);
    Color COLOR_OF_THE_MONTH = new Color(197, 1, 126);

    Pattern IMAGE_EXTENSION_PATTERN = Constants.compileWords("png|jpeg|jpg|gif|bmp|svg|webp|avif|ico|tiff");

    static boolean urlIsImage(String url) {
        String fileExtension = FilenameUtils.getExtension(url);
        return Constants.IMAGE_EXTENSION_PATTERN.matcher(fileExtension).find();
    }

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

    static Pattern compileWords(String s) {
        return Pattern.compile("\\b(" + s + ")\\b", Pattern.CASE_INSENSITIVE);
    }

    static void checkUserTriggerWords(MessageReceivedEvent event, Multimap<String, Pair<Pattern, String>> userTriggerWords) {
        Collection<Pair<Pattern, String>> userTriggers = userTriggerWords.get(event.getAuthor().getId());
        if (CollectionUtils.isNotEmpty(userTriggers)) {
            Optional<String> match = userTriggers.stream().filter(t -> t.getLeft().matcher(event.getMessage().getContentRaw()).find()).map(Pair::getRight).findAny();

            match.ifPresent(s -> event.getJDA().getGuilds().get(0).getTextChannelById(event.getChannel().getId()).sendMessage(s).queue());
        }
    }

    static void debugToTestChannel(JDA jda, String msg) {
        logger.info(msg);
        jda.getTextChannelsByName(Constants.TEST_CHANNEL, false)
                .stream()
                .findFirst()
                .ifPresent(channel -> channel.sendMessage(msg).queue());
    }

    static MessageEmbed simpleEmbedThumbnail(String title, String description, String thumbnail) {
        return simpleEmbed(title, description, null, null, null, thumbnail);
    }

    static MessageEmbed simpleEmbedThumbnail(String title, String description, String image, String thumbnail) {
        return simpleEmbed(title, description, image, null, null, thumbnail);
    }

    static MessageEmbed simpleEmbed(String title, String description) {
        return simpleEmbed(title, description, null, null, null, null);
    }

    static MessageEmbed simpleEmbed(String title, String description, Color color) {
        return simpleEmbed(title, description, null, null, color, null);
    }

    static MessageEmbed simpleEmbed(String title, String description, String image) {
        return simpleEmbed(title, description, image, null, null, null);
    }

    static MessageEmbed simpleEmbed(String title, String description, String image, Color color) {
        return simpleEmbed(title, description, image, null, color, null);
    }

    static MessageEmbed simpleEmbed(String title, String description, String image, String footer) {
        return simpleEmbed(title, description, image, footer, null, null);
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
                .setColor(color != null ? color : COLOR_OF_THE_MONTH)
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
            "powered by 777 rainbows",
            "powered by 777 droughts",
            "powered by 777 WHY THE FUCK IS IT DARK AT 4PMs",
            "powered by 777 buckets",
            "powered by 777 glo ups",
            "powered by 777",
            "powered by 777 777s",
            "powered by 777 chuck norris",
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
            "powered by 777 keanus",
            "powered by 777 behrads",
            "powered by 777 tonys",
            "powered by 777 tonis",
            "powered by 777 meeseeks",
            "powered by 777 stonks",
            "powered by 777 stonkys",
            "powered by 777 shronks",
            "powered by 777 shronky",
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
            "powered by 777 units of electricity",
            "powered by 777 daddys",
            "powered by 777 \uD83D\uDC40",
            "powered by 777 eggs",
            "powered by 777 buffets",
            "powered by 777 maps",
            "powered by 777 trivia questions",
            "powered by 777 tumblrs",
            "powered by 777 tweets",
            "powered by 777 twitters",
            "powered by 777 videos",
            "powered by 777 gifs",
            "powered by 777 shrimp",
            "powered by 777 shremp",
            "powered by 777 kitties",
            "powered by 777 doodles",
            "powered by 777 nuggets",
            "powered by 777 dongles",
            "powered by 777 shrekonings",
            "powered by 777 jigsaws",
            "powered by 777 fanfics",
            "powered by 777 books",
            "powered by 777 newspapers",
            "powered by 777 \uD83C\uDF64",
            "powered by 777 \uD83C\uDF71",
            "powered by 777 ✨",
            "powered by 777 \uD83D\uDD25",
            "powered by 777 \uD83E\uDD7A",
            "powered by 777 santa slots teases",
            "powered by 777 questionable decisions",
            "powered by 777 questions",
            "powered by 777 answers",
            "powered by 777 cookies",
            "powered by 777 biscuits",
            "powered by 777 scones",
            "powered by 777 pastries",
            "powered by 777 taxes",
            "powered by 777 robinhoods",
            "powered by 777 phones",
            "powered by 777 canadian servers",
            "powered by 777 uwus",
            "powered by 777 dongle youtubers"
    );

}
