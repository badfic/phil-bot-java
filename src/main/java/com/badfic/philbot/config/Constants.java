package com.badfic.philbot.config;

import com.badfic.philbot.data.SwampyGamesConfigDal;
import com.badfic.philbot.listeners.DiscordWebhookSendService;
import com.badfic.philbot.service.RandomNumberService;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import jakarta.annotation.PostConstruct;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Constants {
    private static volatile Constants SINGLETON;

    private static final Long2ObjectMap<Function<MessageReactionAddEvent, Boolean>> OUTSTANDING_REACTION_TASKS = Long2ObjectMaps.synchronize(new Long2ObjectArrayMap<>());

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

    public static final Pattern IMAGE_EXTENSION_PATTERN = Constants.compileWords("png|jpeg|jpg|gif|bmp|svg|webp|avif|ico|tiff");

    public static final Short DATA_SINGLETON_ID = 1;

    private final SwampyGamesConfigDal swampyGamesConfigDal;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final RandomNumberService randomNumberService;

    @PostConstruct
    public void init() {
        SINGLETON = this;
    }

    public static void addReactionTask(final long messageId, final Function<MessageReactionAddEvent, Boolean> function) {
        OUTSTANDING_REACTION_TASKS.put(messageId, function);

        SINGLETON.taskScheduler.schedule(() -> {
            OUTSTANDING_REACTION_TASKS.remove(messageId);
        }, Instant.now().plus(15, ChronoUnit.MINUTES));
    }

    public static void computeReactionTask(final MessageReactionAddEvent event) {
        OUTSTANDING_REACTION_TASKS.computeIfPresent(event.getMessageIdLong(), (key, function) -> {
            final var taskIsComplete = function.apply(event);

            if (taskIsComplete) {
                return null;
            }
            return function;
        });
    }

    public static Color colorOfTheMonth() {
        final var colors = SINGLETON.swampyGamesConfigDal.get().getMonthlyColors();
        final var color = pickRandom(colors);
        return Color.decode(color);
    }

    public static boolean isUrl(final String string) {
        if (StringUtils.isBlank(string)) {
            return false;
        }

        try {
            final var uri = URI.create(string);
            return Objects.nonNull(uri.toString());
        } catch (final Exception ignored) {
            return false;
        }
    }

    public static Optional<String> getFilenameExtension(final String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    public static boolean urlIsImage(final String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }

        final var fileExtension = getFilenameExtension(url);

        return fileExtension.map(ext -> Constants.IMAGE_EXTENSION_PATTERN.matcher(ext).find()).orElse(false);
    }

    public static String imageUrlOrElseNull(final String url) {
        return urlIsImage(url) ? url : null;
    }

    public static BufferedImage scaleImageUrlTo(final int width, final int height, final String imageUrl) throws Exception {
        if (width <= 0) {
            return null;
        }

        final var image = ImageIO.read(URI.create(imageUrl).toURL());

        if (image.getWidth() == width && image.getHeight() == height) {
            return image;
        }

        final var scaledTmp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        final var scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        final var graphics = scaledImage.createGraphics();
        graphics.drawImage(scaledTmp, 0, 0, null);
        graphics.dispose();

        return scaledImage;
    }

    public static Optional<Role> hasRole(final Member member, final String roleName) {
        return member.getRoles().stream().filter(r -> r.getName().equalsIgnoreCase(roleName)).findFirst();
    }

    public static <T> T pickRandom(final Collection<T> collection) {
        final var index = SINGLETON.randomNumberService.nextInt(collection.size());
        if (collection instanceof List<T> list) {
            return list.get(index);
        }

        final var iterator = collection.iterator();
        for (var i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    public static <T> T pickRandom(final T[] collection) {
        final var index = SINGLETON.randomNumberService.nextInt(collection.length);
        return collection[index];
    }

    public static String prettyPrintDuration(final Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

    public static Pattern compileWords(final String s) {
        return Pattern.compile("\\b(" + s + ")\\b", Pattern.CASE_INSENSITIVE);
    }

    public static ObjectIntPair<DayOfWeek> isoDayOfWeekMode(final int[] array) {
        if (ArrayUtils.isEmpty(array)) {
            return ObjectIntPair.of(DayOfWeek.SUNDAY, 0);
        }

        var mode = 1;
        var maxCount = 0;

        final var counts = new int[7];

        for (final var currentValue : array) {
            // ISO days are 1 through 7, so we subtract 1 for our counts array indexing
            final var currentFrequency = counts[currentValue - 1]++;
            if (currentFrequency > maxCount) {
                maxCount = currentFrequency;
                mode = currentValue;
            }
        }

        return ObjectIntPair.of(DayOfWeek.of(mode), maxCount);
    }

    public static void checkUserTriggerWords(final MessageReceivedEvent event,
                                             final Long2ObjectMap<List<Pair<Pattern, String>>> userTriggerWords,
                                             final String username,
                                             final String avatar,
                                             final DiscordWebhookSendService discordWebhookSendService) {
        final var userTriggers = userTriggerWords.get(event.getAuthor().getIdLong());
        if (CollectionUtils.isNotEmpty(userTriggers)) {
            var match = Optional.<String>empty();

            for (final var t : userTriggers) {
                if (t.left().matcher(event.getMessage().getContentRaw()).find()) {
                    final var right = t.right();
                    match = Optional.of(right);
                    break;
                }
            }

            match.ifPresent(s -> {
                if (username == null) {
                    event.getJDA().getTextChannelById(event.getChannel().getIdLong()).sendMessage(s).queue();
                    return;
                }

                discordWebhookSendService.sendMessage(event.getChannel().getIdLong(), username, avatar, s);
            });
        }
    }

    public static void debugToTestChannel(final JDA jda, final String msg) {
        log.info(msg);
        jda.getTextChannelsByName(Constants.TEST_CHANNEL, false).getFirst().sendMessage(msg).queue();
    }

    public static void debugToModLogsChannel(final JDA jda, final MessageEmbed messageEmbed) {
        jda.getTextChannelsByName(Constants.MOD_LOGS_CHANNEL, false).getFirst().sendMessageEmbeds(messageEmbed).queue();
    }

    public static MessageEmbed simpleEmbedThumbnail(final String title, final String description, final String thumbnail) {
        return simpleEmbed(title, description, null, null, null, thumbnail);
    }

    public static MessageEmbed simpleEmbedThumbnail(final String title, final String description, final String image, final String thumbnail) {
        return simpleEmbed(title, description, image, null, null, thumbnail);
    }

    public static MessageEmbed simpleEmbed(final String title, final String description) {
        return simpleEmbed(title, description, null, null, null, null);
    }

    public static MessageEmbed simpleEmbed(final String title, final String description, final Color color) {
        return simpleEmbed(title, description, null, null, color, null);
    }

    public static MessageEmbed simpleEmbed(final String title, final String description, final String image) {
        return simpleEmbed(title, description, image, null, null, null);
    }

    public static MessageEmbed simpleEmbed(final String title, final String description, final String image, final Color color) {
        return simpleEmbed(title, description, image, null, color, null);
    }

    public static MessageEmbed simpleEmbed(final String title, final String description, final String image, final String footer) {
        return simpleEmbed(title, description, image, footer, null, null);
    }

    public static MessageEmbed simpleEmbed(final String title, final String description, final String image, final String footer, final Color color) {
        return simpleEmbed(title, description, image, footer, color, null);
    }

    public static MessageEmbed simpleEmbed(final String title, final String description, final String image, final String footer, final Color color, final String thumbnail) {
        return simpleEmbed(title, description, image, footer, color, thumbnail, true);
    }

    public static MessageEmbed simpleEmbed(final String title, final String description, final String image, final String footer, final Color color, final String thumbnail, final boolean include777) {
        final String finalDesc;
        if (StringUtils.isNotBlank(description)) {
            finalDesc = description.length() > 2048 ? (description.substring(0, 2044) + "...") : description;
        } else {
            finalDesc = null;
        }

        var fullFooter = footer;
        if (include777) {
            final var footers = SINGLETON.swampyGamesConfigDal.get().getEmbedFooters();
            final var footerAddition = pickRandom(footers);

            fullFooter = fullFooter != null ? (fullFooter + "\n" + footerAddition) : footerAddition;
            fullFooter = fullFooter.length() > 2048 ? (fullFooter.substring(0, 2044) + "777") : fullFooter;
        } else if (fullFooter != null && fullFooter.length() > 2048) {
            fullFooter = fullFooter.substring(0, 2044) + "...";
        }

        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(finalDesc)
                .setImage(imageUrlOrElseNull(image))
                .setColor(color != null ? color : colorOfTheMonth())
                .setFooter(fullFooter)
                .setThumbnail(imageUrlOrElseNull(thumbnail))
                .build();
    }

}
