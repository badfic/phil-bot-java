package com.badfic.philbot.config;

import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.SwampyGamesConfigRepository;
import com.google.common.collect.Multimap;
import jakarta.annotation.PostConstruct;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Constants {
    private static Constants SINGLETON;

    public static final String PREFIX = "!!";

    public static final String ADMIN_ROLE = "Queens of the Castle";
    public static final String EIGHTEEN_PLUS_ROLE = "18+";
    public static final String CHAOS_CHILDREN_ROLE = "Chaos Children";
    public static final String SWAMPYS_CHANNEL = "the-swampys";
    public static final String MEGA_HELL_CHANNEL = "mega-hell";
    public static final String MEGA_HELL_ROLE = "in mega hell";
    public static final String TEST_CHANNEL = "test-channel";
    public static final String MOD_LOGS_CHANNEL = "super-secret-mod-announcements";
    public static final String HUNGERDOME_CHANNEL = "the-hungerdome";
    public static final String CURSED_SWAMP_CHANNEL = "cursed-swamp";
    public static final String MEMES_CHANNEL = "memes";

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36";

    public static final Color SWAMP_GREEN = new Color(89, 145, 17);

    public static final Pattern IMAGE_EXTENSION_PATTERN = Constants.compileWords("png|jpeg|jpg|gif|bmp|svg|webp|avif|ico|tiff");

    @Getter
    private final SwampyGamesConfigRepository swampyGamesConfigRepository;

    @PostConstruct
    public void init() {
        SINGLETON = this;
    }

    public static Color colorOfTheMonth() {
        return SINGLETON.swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID).map(swampyGamesConfig -> {
            String color = pickRandom(swampyGamesConfig.getMonthlyColors());
            return Color.decode(color);
        }).orElse(SWAMP_GREEN);
    }

    public static boolean isUrl(String string) {
        if (StringUtils.isBlank(string)) {
            return false;
        }

        try {
            URL url = new URL(string);
            URI uri = url.toURI();
            return Objects.nonNull(uri.toString());
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean urlIsImage(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }

        String fileExtension = FilenameUtils.getExtension(url);
        return Constants.IMAGE_EXTENSION_PATTERN.matcher(fileExtension).find();
    }

    public static String imageUrlOrElseNull(String url) {
        return urlIsImage(url) ? url : null;
    }

    public static BufferedImage scaleImageUrlTo(int width, int height, String imageUrl) throws Exception {
        if (width <= 0) {
            return null;
        }

        BufferedImage image = ImageIO.read(new URL(imageUrl));

        if (image.getWidth() == width && image.getHeight() == height) {
            return image;
        }

        Image scaledTmp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = scaledImage.createGraphics();
        graphics.drawImage(scaledTmp, 0, 0, null);
        graphics.dispose();

        return scaledImage;
    }

    public static Optional<Role> hasRole(Member member, String roleName) {
        return member.getRoles()
                .stream()
                .filter(r -> r.getName().equalsIgnoreCase(roleName))
                .findAny();
    }

    public static <T> T pickRandom(Collection<T> collection) {
        int index = ThreadLocalRandom.current().nextInt(collection.size());
        Iterator<T> iterator = collection.iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    public static <T> T pickRandom(T[] collection) {
        int index = ThreadLocalRandom.current().nextInt(collection.length);
        return collection[index];
    }

    public static String prettyPrintDuration(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

    public static Pattern compileWords(String s) {
        return Pattern.compile("\\b(" + s + ")\\b", Pattern.CASE_INSENSITIVE);
    }

    public static Pair<DayOfWeek, Integer> isoDayOfWeekMode(int[] array) {
        if (ArrayUtils.isEmpty(array)) {
            return ImmutablePair.of(DayOfWeek.SUNDAY, 0);
        }

        int mode = 1;
        int maxCount = 0;

        int[] counts = new int[7];

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < array.length; i++) {
            int currentValue = array[i];

            // ISO days are 1 through 7, so we subtract 1 for our counts array indexing
            int currentFrequency = counts[currentValue - 1]++;
            if (currentFrequency > maxCount) {
                maxCount = currentFrequency;
                mode = currentValue;
            }
        }

        return ImmutablePair.of(DayOfWeek.of(mode), maxCount);
    }

    public static void checkUserTriggerWords(MessageReceivedEvent event, Multimap<String, Pair<Pattern, String>> userTriggerWords) {
        Collection<Pair<Pattern, String>> userTriggers = userTriggerWords.get(event.getAuthor().getId());
        if (CollectionUtils.isNotEmpty(userTriggers)) {
            Optional<String> match = userTriggers.stream().filter(t -> t.getLeft().matcher(event.getMessage().getContentRaw()).find()).map(Pair::getRight).findAny();

            match.ifPresent(s -> event.getJDA().getTextChannelById(event.getChannel().getId()).sendMessage(s).queue());
        }
    }

    public static void debugToTestChannel(JDA jda, String msg) {
        log.info(msg);
        jda.getTextChannelsByName(Constants.TEST_CHANNEL, false)
                .stream()
                .findFirst()
                .ifPresent(channel -> channel.sendMessage(msg).queue());
    }

    public static void debugToModLogsChannel(JDA jda, MessageEmbed messageEmbed) {
        jda.getTextChannelsByName(Constants.MOD_LOGS_CHANNEL, false)
                .stream()
                .findAny()
                .ifPresent(channel -> channel.sendMessageEmbeds(messageEmbed).queue());
    }

    public static MessageEmbed simpleEmbedThumbnail(String title, String description, String thumbnail) {
        return simpleEmbed(title, description, null, null, null, thumbnail);
    }

    public static MessageEmbed simpleEmbedThumbnail(String title, String description, String image, String thumbnail) {
        return simpleEmbed(title, description, image, null, null, thumbnail);
    }

    public static MessageEmbed simpleEmbed(String title, String description) {
        return simpleEmbed(title, description, null, null, null, null);
    }

    public static MessageEmbed simpleEmbed(String title, String description, Color color) {
        return simpleEmbed(title, description, null, null, color, null);
    }

    public static MessageEmbed simpleEmbed(String title, String description, String image) {
        return simpleEmbed(title, description, image, null, null, null);
    }

    public static MessageEmbed simpleEmbed(String title, String description, String image, Color color) {
        return simpleEmbed(title, description, image, null, color, null);
    }

    public static MessageEmbed simpleEmbed(String title, String description, String image, String footer) {
        return simpleEmbed(title, description, image, footer, null, null);
    }

    public static MessageEmbed simpleEmbed(String title, String description, String image, String footer, Color color) {
        return simpleEmbed(title, description, image, footer, color, null);
    }

    public static MessageEmbed simpleEmbed(String title, String description, String image, String footer, Color color, String thumbnail) {
        final String finalDesc;
        if (StringUtils.isNotBlank(description)) {
            finalDesc = description.length() > 2048 ? (description.substring(0, 2044) + "...") : description;
        } else {
            finalDesc = null;
        }

        String footerAddition = SINGLETON.swampyGamesConfigRepository
                .findById(SwampyGamesConfig.SINGLETON_ID)
                .map(swampyGamesConfig -> pickRandom(swampyGamesConfig.getEmbedFooters()))
                .orElse("powered by 777");

        footer = footer != null ? (footer + "\n" + footerAddition) : footerAddition;
        footer = footer.length() > 2048 ? (footer.substring(0, 2044) + "777") : footer;

        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(finalDesc)
                .setImage(imageUrlOrElseNull(image))
                .setColor(color != null ? color : colorOfTheMonth())
                .setFooter(footer)
                .setThumbnail(imageUrlOrElseNull(thumbnail))
                .build();
    }

}
