package com.badfic.philbot.listeners.phil;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.Rank;
import com.badfic.philbot.repository.DiscordUserRepository;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.awt.Color;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SwampyCommand extends Command implements PhilMarker {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // Timeouts
    public static final long PICTURE_MSG_BONUS_TIMEOUT_MINUTES = 3;
    public static final long SLOTS_TIMEOUT_MINUTES = 3;
    public static final long STEAL_TIMEOUT_MINUTES = 60;
    public static final long STEAL_REACTION_TIME_MINUTES = 5;

    // message/vc/emote points
    public static final long NORMAL_MSG_POINTS = 5;
    public static final long CURSED_MSG_POINTS = 10;
    public static final Set<String> CURSED_MSG_CHANNELS = new HashSet<>(Arrays.asList(
            "cursed-swamp",
            "nate-heywoods-simp-hour",
            "thirsty-legends",
            "gay-receipts"
    ));
    public static final long PICTURE_MSG_POINTS = 150;
    public static final long CURSED_PICTURE_MSG_POINTS = 250;
    public static final long NORMAL_REACTION_POINTS = 2;
    public static final long EMOTE_REACTION_POINTS = 7;
    public static final long VOICE_CHAT_POINTS_PER_MINUTE = 5;

    // upvote/downvote points
    public static final long UPVOTE_TIMEOUT_MINUTES = 1;
    public static final long DOWNVOTE_TIMEOUT_MINUTES = 1;
    public static final long UPVOTE_POINTS_TO_UPVOTEE = 500;
    public static final long UPVOTE_POINTS_TO_UPVOTER = 250;
    public static final long DOWNVOTE_POINTS_FROM_DOWNVOTEE = 100;
    public static final long DOWNVOTE_POINTS_TO_DOWNVOTER = 50;

    // sweepstakes, taxes, robinhood
    public static final BigDecimal ONE_HUNDREDTH = new BigDecimal("0.01");
    public static final long TAX_THRESHOLD = 100;
    public static final long ORGANIC_POINT_THRESHOLD = 1_000;
    public static final long SWEEPSTAKES_WIN_POINTS = 3_000;
    public static final Pair<Integer, Integer> TAX_PERCENTAGE_MIN_MAX = ImmutablePair.of(5, 16);
    public static final Pair<Integer, Integer> ROBINHOOD_PERCENTAGE_MIN_MAX = ImmutablePair.of(5, 16);
    public static final long PERCENT_CHANCE_TAXES_DOESNT_HAPPEN = 30;
    public static final long PERCENT_CHANCE_ROBINHOOD_DOESNT_HAPPEN = 30;

    // swiper and boost
    public static final long SWIPER_POINTS_TO_STEAL = 1_500;
    public static final long BOOST_POINTS_TO_GIVE = 1_000;
    public static final long PERCENTAGE_CHANCE_BOOST_HAPPENS_ON_THE_HOUR = 15;

    // slots
    public static final long SLOTS_WIN_POINTS = 10_000;
    public static final long SLOTS_TWO_OUT_OF_THREE_POINTS = 50;

    // images
    private static final String BOOST_START = "https://cdn.discordapp.com/attachments/323666308107599872/761492230379798538/BOOST.png";
    private static final String BOOST_END = "https://cdn.discordapp.com/attachments/323666308107599872/761494374445219850/stan_loona_goddess.png";

    private static final String TAXES = "https://cdn.discordapp.com/attachments/323666308107599872/761472008734244864/martha_taxes.png";
    private static final String PERSON_WHO_STOPS_TAXES = "https://cdn.discordapp.com/attachments/323666308107599872/761473370604568606/snoop_no_taxes.png";

    private static final String ROBINHOOD = "https://cdn.discordapp.com/attachments/323666308107599872/761475204702535680/oprah_refund_robinhood.png";
    private static final String PERSON_WHO_STOPS_ROBINHOOD = "https://cdn.discordapp.com/attachments/323666308107599872/761477965586366484/george_lopez_stop_oprah.png";

    private static final String SWEEPSTAKES = "https://cdn.discordapp.com/attachments/323666308107599872/761467155333644298/sweepstakes_cats.png";

    private static final String SWIPER_SPOTTED = "https://cdn.discordapp.com/attachments/323666308107599872/761495531498373120/swiper-spotted.jpg";
    private static final String SWIPER_WON = "https://cdn.discordapp.com/attachments/323666308107599872/761495329496498176/stephen-molyneaux-dora-swiper-1001.png";
    private static final String NO_SWIPING = "https://cdn.discordapp.com/attachments/707453916882665552/757776008639283321/unknown.png";
    private static final String SNART_SPOTTED = "https://cdn.discordapp.com/attachments/323666308107599872/758910821950029824/snart_rory.png";
    private static final String SNART_WON = "https://cdn.discordapp.com/attachments/323666308107599872/758911981176094801/snart_rory_mischief_managed.png";
    private static final String NO_SNART = "https://cdn.discordapp.com/attachments/323666308107599872/758911984925802516/snart_rory_you_cant_steal_here.png";

    // emoji
    private static final String[] LEADERBOARD_MEDALS = {
            "\uD83E\uDD47", "\uD83E\uDD48", "\uD83E\uDD49",
            "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40", "\uD83D\uDC40"
    };
    private static final String SLOT_MACHINE = "\uD83C\uDFB0";
    private static final Set<String> SPOOKY_SLOTS = new HashSet<>(Arrays.asList(
            "\uD83C\uDF83", "\uD83D\uDC7B", "\uD83D\uDC80", "\uD83C\uDF42", "\uD83C\uDF15",
            "\uD83E\uDDDB", "\uD83E\uDDDF", "\uD83D\uDD77️", "\uD83E\uDD87", "\uD83C\uDF6C"
    ));
    private static final Set<String> SLOTS = new HashSet<>(Arrays.asList(
            "\uD83E\uDD5D", "\uD83C\uDF53", "\uD83C\uDF4B", "\uD83E\uDD6D", "\uD83C\uDF51",
            "\uD83C\uDF48", "\uD83C\uDF4A", "\uD83C\uDF4D", "\uD83C\uDF50", "\uD83C\uDF47"
    ));

    // volatile state
    private volatile String swiperAwaiting = null;
    private volatile boolean didSomeoneSaveFromSwiper = false;
    private volatile String noSwipingPhrase = "Swiper No Swiping";
    private volatile boolean boostAwaiting = false;
    private volatile String boostPhrase = "boost";
    private volatile boolean awaitingResetConfirmation = false;

    // non volatile state
    private final DiscordUserRepository discordUserRepository;
    private final String userHelp;
    private final String adminHelp;
    private final ScheduledExecutorService scheduler;

    @Resource(name = "philJda")
    @Lazy
    private JDA philJda;

    @Autowired
    public SwampyCommand(DiscordUserRepository discordUserRepository) {
        name = "swampy";
        aliases = new String[] {"bastard", "spooky"};
        userHelp =
                "`!!swampy` aka `!!bastard` aka `!!spooky` HELP:\n" +
                "`!!swampy rank` show your swampy rank\n" +
                "`!!swampy leaderboard` show the swampy leaderboard\n" +
                "`!!swampy up @incogmeato` upvote a user for the swampys\n" +
                "`!!swampy down @incogmeato` downvote a user for the swampys\n" +
                "`!!swampy slots` Play slots. Winners for 2 out of 3 matches or 3 out of 3 matches.";
        adminHelp = userHelp + "\n\nMODS ONLY COMMANDS:\n" +
                "`!!swampy give 120 @incogmeato` give 120 points to incogmeato\n" +
                "`!!swampy take 120 @incogmeato` remove 120 points from incogmeato\n" +
                "`!!swampy set 120 @incogmeato` set incogmeato to 120 points\n" +
                "`!!swampy reset` reset everyone back to level 0";
        this.discordUserRepository = discordUserRepository;
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Scheduled(cron = "0 0 2 * * ?", zone = "GMT")
    public void sweepstakes() {
        List<DiscordUser> allUsers = discordUserRepository.findAll();

        long startTime = System.currentTimeMillis();
        Member member = null;
        while (member == null && System.currentTimeMillis() - startTime < TimeUnit.SECONDS.toMillis(30)) {
            DiscordUser winningUser = pickRandom(allUsers);

            try {
                Member memberById = philJda.getGuilds().get(0).retrieveMemberById(winningUser.getId()).complete();
                if (memberById != null
                        && !memberById.getUser().isBot()
                        && winningUser.getXp() > ORGANIC_POINT_THRESHOLD
                        && winningUser.getUpdateTime().isAfter(LocalDateTime.now().minusHours(24))) {
                    member = memberById;
                }
            } catch (Exception ignored) {}
        }

        if (member == null) {
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(simpleEmbed("Sweepstakes Results", "Unable to choose a winner, nobody wins"))
                    .queue();
            return;
        }

        givePointsToMember(SWEEPSTAKES_WIN_POINTS, member);

        MessageEmbed message = new EmbedBuilder()
                .setTitle("Sweepstakes Results")
                .setImage(SWEEPSTAKES)
                .setColor(Color.GREEN)
                .setDescription(String.format("Congratulations %s you won today's sweepstakes worth 4000 points!", member.getAsMention()))
                .build();

        philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                .get(0)
                .sendMessage(message)
                .queue();
    }

    @Scheduled(cron = "0 5 2 * * ?", zone = "GMT")
    public void taxes() {
        if (ThreadLocalRandom.current().nextInt(100) < PERCENT_CHANCE_TAXES_DOESNT_HAPPEN) {
            MessageEmbed message = new EmbedBuilder()
                    .setTitle("No taxes today!")
                    .setDescription("Snoop Dogg caught Martha Stewart before she could take taxes from the swamp.")
                    .setImage(PERSON_WHO_STOPS_TAXES)
                    .setColor(Color.RED)
                    .build();

            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
            return;
        }

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        allUsers.sort((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())); // Descending sort

        long totalTaxes = 0;
        StringBuilder description = new StringBuilder();
        for (DiscordUser user : allUsers) {
            if (user.getXp() > TAX_THRESHOLD) {
                try {
                    long taxRate = ThreadLocalRandom.current().nextInt(TAX_PERCENTAGE_MIN_MAX.getLeft(), TAX_PERCENTAGE_MIN_MAX.getRight());
                    long taxes = BigDecimal.valueOf(user.getXp()).multiply(ONE_HUNDREDTH).multiply(BigDecimal.valueOf(taxRate)).longValue();
                    totalTaxes += taxes;
                    Member memberById = philJda.getGuilds().get(0).retrieveMemberById(user.getId()).complete();
                    if (memberById != null && !memberById.getUser().isBot()) {
                        takePointsFromMember(taxes, memberById);

                        description
                                .append("Collected ")
                                .append(NumberFormat.getIntegerInstance().format(taxes))
                                .append(" points (")
                                .append(taxRate)
                                .append("%) from <@!")
                                .append(user.getId())
                                .append(">\n");
                    }
                } catch (Exception e) {
                    logger.error("Failed to tax user [id={}]", user.getId(), e);
                }
            }
        }

        long startTime = System.currentTimeMillis();
        int index = allUsers.size() - 1;
        Member member = null;
        while (index >= 0 && member == null && System.currentTimeMillis() - startTime < TimeUnit.SECONDS.toMillis(30)) {
            DiscordUser winningUser = allUsers.get(index);

            try {
                Member memberById = philJda.getGuilds().get(0).retrieveMemberById(winningUser.getId()).complete();
                if (memberById != null
                        && !memberById.getUser().isBot()
                        && winningUser.getXp() > ORGANIC_POINT_THRESHOLD
                        && winningUser.getUpdateTime().isAfter(LocalDateTime.now().minusHours(24))) {
                    member = memberById;
                }
            } catch (Exception ignored) {}

            index--;
        }

        String title = "Tax time! The following taxes have been paid to the luminiferous aether";
        if (member != null) {
            title = "Tax time! The following taxes have been paid to " + member.getEffectiveName();
            givePointsToMember(totalTaxes, member);
        }

        MessageEmbed message = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description.toString())
                .setImage(TAXES)
                .setColor(Color.RED)
                .build();

        philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                .get(0)
                .sendMessage(message)
                .queue();
    }

    @Scheduled(cron = "0 15 2 * * ?", zone = "GMT")
    public void robinhood() {
        if (ThreadLocalRandom.current().nextInt(100) < PERCENT_CHANCE_ROBINHOOD_DOESNT_HAPPEN) {
            MessageEmbed message = new EmbedBuilder()
                    .setTitle("Wapa!!!")
                    .setDescription("George Lopez caught Oprah while she was trying to return taxes to the swamp.")
                    .setImage(PERSON_WHO_STOPS_ROBINHOOD)
                    .setColor(Color.RED)
                    .build();

            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
            return;
        }

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        allUsers.sort((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())); // Descending sort

        long totalRecovered = 0;
        StringBuilder description = new StringBuilder();
        for (DiscordUser user : allUsers) {
            if (user.getXp() > TAX_THRESHOLD) {
                try {
                    long taxRateRecoveryAmountPercentage = ThreadLocalRandom.current().nextInt(ROBINHOOD_PERCENTAGE_MIN_MAX.getLeft(), ROBINHOOD_PERCENTAGE_MIN_MAX.getRight());
                    long recoveredTaxes = BigDecimal.valueOf(user.getXp()).multiply(ONE_HUNDREDTH).multiply(BigDecimal.valueOf(taxRateRecoveryAmountPercentage)).longValue();
                    totalRecovered += recoveredTaxes;
                    Member memberById = philJda.getGuilds().get(0).retrieveMemberById(user.getId()).complete();
                    if (memberById != null && !memberById.getUser().isBot()) {
                        givePointsToMember(recoveredTaxes, memberById);

                        description
                                .append("Recovered ")
                                .append(NumberFormat.getIntegerInstance().format(recoveredTaxes))
                                .append(" points (")
                                .append(taxRateRecoveryAmountPercentage)
                                .append("%) to <@!")
                                .append(user.getId())
                                .append(">\n");
                    }
                } catch (Exception e) {
                    logger.error("Failed to robinhood user [id={}]", user.getId(), e);
                }
            }
        }

        description.append("\nI recovered a total of ")
                .append(NumberFormat.getIntegerInstance().format(totalRecovered))
                .append(" points and gave them back to the swamp!");

        String title = "Robinhood! The following taxes have been returned";
        MessageEmbed message = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description.toString())
                .setImage(ROBINHOOD)
                .setColor(Color.ORANGE)
                .build();

        philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                .get(0)
                .sendMessage(message)
                .queue();
    }

    @Scheduled(cron = "0 20 0,2,4,6,8,10,12,14,16,18,20,22 * * ?", zone = "GMT")
    public void swiper() {
        if (swiperAwaiting != null) {
            Optional<DiscordUser> discordUser = discordUserRepository.findById(swiperAwaiting);
            swiperAwaiting = null;

            MessageEmbed message = new EmbedBuilder()
                    .setTitle(noSwipingPhrase)
                    .setDescription("Congratulations, <@!" + philJda.getSelfUser().getId() + "> is a moron so nobody loses any points")
                    .setColor(Color.GREEN)
                    .setImage(NO_SWIPING)
                    .build();

            if (discordUser.isPresent()) {
                if (didSomeoneSaveFromSwiper) {
                    didSomeoneSaveFromSwiper = false;

                    message = new EmbedBuilder()
                            .setTitle(noSwipingPhrase)
                            .setDescription("Congratulations, you scared them away from <@!" + discordUser.get().getId() + ">")
                            .setColor(Color.GREEN)
                            .setImage(StringUtils.containsIgnoreCase(noSwipingPhrase, "swiper") ? NO_SWIPING : NO_SNART)
                            .build();
                } else {
                    try {
                        Member memberById = philJda.getGuilds().get(0).retrieveMemberById(discordUser.get().getId()).complete();

                        if (memberById != null) {
                            takePointsFromMember(SWIPER_POINTS_TO_STEAL, memberById);
                            message = new EmbedBuilder()
                                    .setTitle(StringUtils.containsIgnoreCase(noSwipingPhrase, "swiper") ? "Swiper Escaped!" : "Rory and Snart Escaped!")
                                    .setDescription("You didn't save <@!" + discordUser.get().getId() + "> in time, they lost " + SWIPER_POINTS_TO_STEAL + " points")
                                    .setColor(Color.RED)
                                    .setImage(StringUtils.containsIgnoreCase(noSwipingPhrase, "swiper") ? SWIPER_WON : SNART_WON)
                                    .build();
                        }
                    } catch (Exception e) {
                        logger.error("Exception looking up swiper victim [id={}] after they were not saved", discordUser.get().getId(), e);
                    }
                }
            }

            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
            return;
        }

        List<DiscordUser> allUsers = discordUserRepository.findAll();
        Collections.shuffle(allUsers);

        long startTime = System.currentTimeMillis();
        Member member = null;
        while (member == null && System.currentTimeMillis() - startTime < TimeUnit.SECONDS.toMillis(30)) {
            DiscordUser winningUser = pickRandom(allUsers);

            try {
                Member memberById = philJda.getGuilds().get(0).retrieveMemberById(winningUser.getId()).complete();
                if (memberById != null
                        && !memberById.getUser().isBot()
                        && winningUser.getXp() > SWIPER_POINTS_TO_STEAL
                        && winningUser.getUpdateTime().isAfter(LocalDateTime.now().minusHours(24))) {
                    member = memberById;
                }
            } catch (Exception ignored) {}
        }

        if (member == null) {
            MessageEmbed message = new EmbedBuilder()
                    .setTitle("Swiper Was Spotted Nearby")
                    .setDescription("Swiper was spotted nearby, be on the lookout for him")
                    .setColor(Color.GREEN)
                    .setImage(NO_SWIPING)
                    .build();

            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
            return;
        }

        long delay = pickRandom(Arrays.asList(15L, 30L, 45L));
        boolean swiper = ThreadLocalRandom.current().nextInt() % 2 == 0;
        swiperAwaiting = member.getId();
        noSwipingPhrase = swiper ? "Swiper No Swiping" : "Snarter No Snarting";
        scheduler.schedule(this::swiper, delay, TimeUnit.MINUTES);

        MessageEmbed message = new EmbedBuilder()
                .setTitle(swiper ? "Swiper Was Spotted Nearby" : "Rory and Snart Were Spotted Nearby")
                .setDescription("They're trying to steal from <@!" + member.getId() + ">\nType '" + noSwipingPhrase + "' in this channel to stop them!")
                .setColor(Color.YELLOW)
                .setImage(swiper ? SWIPER_SPOTTED : SNART_SPOTTED)
                .build();

        philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                .get(0)
                .sendMessage(message)
                .queue();
    }

    @Scheduled(cron = "0 0 * * * ?", zone = "GMT")
    public void boost() {
        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                .get(0);

        if (boostAwaiting) {
            boostAwaiting = false;

            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            StringBuilder description = new StringBuilder();
            discordUserRepository.findAll()
                    .stream()
                    .filter(u -> u.getAcceptedBoost().isAfter(oneHourAgo))
                    .forEach(u -> {
                        try {
                            Member memberLookedUp = philJda.getGuilds().get(0).retrieveMemberById(u.getId()).complete();
                            if (memberLookedUp == null) {
                                throw new RuntimeException("member not found");
                            }

                            givePointsToMember(1000, memberLookedUp);
                            description.append("Gave " + BOOST_POINTS_TO_GIVE + " points to <@!")
                                    .append(u.getId())
                                    .append(">\n");
                        } catch (Exception e) {
                            logger.error("Failed to give boost points to user [id={}]", u.getId(), e);
                            description.append("OOPS: Unable to give points to <@!")
                                    .append(u.getId())
                                    .append(">\n");
                        }
                    });

            MessageEmbed messageEmbed = new EmbedBuilder()
                    .setImage(BOOST_END)
                    .setTitle("Boost Blitz Complete")
                    .setDescription(description.toString())
                    .setColor(Color.GREEN)
                    .build();
            swampysChannel.sendMessage(messageEmbed).queue();
            return;
        }

        if (ThreadLocalRandom.current().nextInt(100) < PERCENTAGE_CHANCE_BOOST_HAPPENS_ON_THE_HOUR) {
            boostAwaiting = true;
            boostPhrase = pickRandom(Arrays.asList("boost", "yeet", "simp", "swamp", "spooner", "grapefruit", "thicc", "ranch", "bounce", "hard", "firm",
                    "loose", "rub", "moist", "wet", "damp", "spooky", "season", "pumpkin", "skeleton", "vampire", "bat", "coffin", "trick", "treat", "harvest",
                    "ghost", "skull", "spirit", "halloween", "costume", "afraid", "cat", "witch", "magic", "boo", "broomstick", "boogeyman", "corpse", "casket",
                    "cloak", "creepy", "devil", "dark", "demon", "prank", "darkness", "evil", "death", "goblin", "zombie", "spider", "fog", "karl", "gruesome",
                    "grave", "goodies", "haunted", "howl", "horror", "spoopy", "horrifying", "macabre", "masquerade", "mist", "moon", "morbid",
                    "midnight", "mummy", "nightmare", "night", "ogre", "october", "petrify", "phantom", "rip", "scarecrow", "scare", "fright", "scary",
                    "scream", "shadow", "startled", "web", "sweets", "supernatural", "tomb", "wand", "wicked", "witch", "wizard", "wraith", "warlock",
                    "sorcerer"));

            MessageEmbed message = new EmbedBuilder()
                    .setTitle("BOOST BLITZ")
                    .setDescription("Type `" + boostPhrase + "` in this channel within the next hour to be boosted by "
                            + BOOST_POINTS_TO_GIVE + " points")
                    .setImage(BOOST_START)
                    .setColor(Color.GREEN)
                    .build();

            swampysChannel.sendMessage(message).queue();
        }
    }

    public void givePointsToMember(long pointsToGive, Member member, DiscordUser user) {
        if (member.getUser().isBot() || hasRole(member, Constants.NEWBIE_ROLE)) {
            return;
        }

        user.setXp(user.getXp() + pointsToGive);
        user = discordUserRepository.save(user);
        assignRolesIfNeeded(member, user);
    }

    public void givePointsToMember(long pointsToGive, Member member) {
        if (member.getUser().isBot() || hasRole(member, Constants.NEWBIE_ROLE)) {
            return;
        }

        givePointsToMember(pointsToGive, member, getDiscordUserByMember(member));
    }

    public void takePointsFromMember(long pointsToTake, Member member) {
        if (member.getUser().isBot() || hasRole(member, Constants.NEWBIE_ROLE)) {
            return;
        }

        DiscordUser user = getDiscordUserByMember(member);
        user.setXp(Math.max(0, user.getXp() - pointsToTake));
        user = discordUserRepository.save(user);
        assignRolesIfNeeded(member, user);
    }

    public void voiceJoined(Member member) {
        if (member.getUser().isBot() || hasRole(member, Constants.NEWBIE_ROLE)) {
            return;
        }

        DiscordUser user = getDiscordUserByMember(member);
        user.setVoiceJoined(LocalDateTime.now());
        discordUserRepository.save(user);
    }

    public void voiceLeft(Member member) {
        if (member.getUser().isBot() || hasRole(member, Constants.NEWBIE_ROLE)) {
            return;
        }

        DiscordUser user = getDiscordUserByMember(member);
        LocalDateTime timeTheyJoinedVoice = user.getVoiceJoined();

        if (timeTheyJoinedVoice == null) {
            return;
        }

        long minutes = ChronoUnit.MINUTES.between(timeTheyJoinedVoice, LocalDateTime.now());
        long points = minutes * VOICE_CHAT_POINTS_PER_MINUTE;
        user.setVoiceJoined(null);
        discordUserRepository.save(user);

        givePointsToMember(points, member);
    }

    public void acceptedBoost(Member member) {
        DiscordUser discordUser = getDiscordUserByMember(member);
        discordUser.setAcceptedBoost(LocalDateTime.now());
        discordUserRepository.save(discordUser);
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getAuthor().isBot() || hasRole(event.getMember(), Constants.NEWBIE_ROLE)) {
            return;
        }

        String args = event.getArgs();
        String msgContent = event.getMessage().getContentRaw();

        if (args.startsWith("help")) {
            if (hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
                event.replyInDm(simpleEmbed("Help", adminHelp));
            } else {
                event.replyInDm(simpleEmbed("Help", userHelp));
            }
        } else if (args.startsWith("rank")) {
            showRank(event);
        } else if (args.startsWith("up")) {
            upvote(event);
        } else if (args.startsWith("down")) {
            downvote(event);
        } else if (args.startsWith("give")) {
            give(event);
        } else if (args.startsWith("take")) {
            take(event);
        } else if (args.startsWith("set")) {
            set(event);
        } else if (args.startsWith("leaderboard")) {
            leaderboard(event);
        } else if (args.startsWith("steal") || args.startsWith("flip")) {
            event.replyError("Both flip and steal are disabled for now, they'll be back soon-ish.");
        } else if (args.startsWith("slots")) {
            slots(event);
        } else if (args.startsWith("reset")) {
            if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
                event.replyError("You do not have permission to use this command");
                return;
            }

            if (!awaitingResetConfirmation) {
                event.reply(simpleEmbed("Reset The Games", "Are you sure you want to reset the Swampys? This will reset everyone's points. If you are sure, type `!!swampy reset confirm`"));
                awaitingResetConfirmation = true;
            } else {
                if (!args.startsWith("reset confirm")) {
                    event.reply(simpleEmbed("Abort Reset", "Swampy reset aborted. You must type `!!swampy reset` and then confirm it with `!!swampy reset confirm`."));
                    awaitingResetConfirmation = false;
                    return;
                }
                reset(event);
                awaitingResetConfirmation = false;
            }
        } else if (msgContent.startsWith("!!")) {
            event.replyError("Unrecognized command");
        } else {
            if (boostAwaiting && StringUtils.containsIgnoreCase(msgContent, boostPhrase) && Constants.SWAMPYS_CHANNEL.equals(event.getChannel().getName())) {
                acceptedBoost(event.getMember());
                return;
            }

            if (swiperAwaiting != null && StringUtils.containsIgnoreCase(msgContent, noSwipingPhrase)
                    && Constants.SWAMPYS_CHANNEL.equals(event.getChannel().getName())) {
                didSomeoneSaveFromSwiper = true;
                return;
            }

            Message message = event.getMessage();

            DiscordUser discordUser = getDiscordUserByMember(event.getMember());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextMsgBonusTime = discordUser.getLastMessageBonus().plus(PICTURE_MSG_BONUS_TIMEOUT_MINUTES, ChronoUnit.MINUTES);

            boolean bonus = CollectionUtils.isNotEmpty(message.getAttachments())
                    || CollectionUtils.isNotEmpty(message.getEmbeds())
                    || CollectionUtils.isNotEmpty(message.getEmotes());

            long pointsToGive = NORMAL_MSG_POINTS;
            if ("bot-space".equals(event.getChannel().getName())) {
                pointsToGive = 1;
            } else if (CURSED_MSG_CHANNELS.contains(event.getChannel().getName())) {
                pointsToGive = CURSED_MSG_POINTS;

                if (bonus && now.isAfter(nextMsgBonusTime)) {
                    if ("cursed-swamp".equalsIgnoreCase(event.getChannel().getName()) || "gay-receipts".equalsIgnoreCase(event.getChannel().getName())) {
                        pointsToGive = CURSED_PICTURE_MSG_POINTS;
                    } else {
                        pointsToGive = PICTURE_MSG_POINTS;
                    }
                    discordUser.setLastMessageBonus(now);
                }
            } else if (bonus && now.isAfter(nextMsgBonusTime)) {
                pointsToGive = PICTURE_MSG_POINTS;
                discordUser.setLastMessageBonus(now);
            }

            givePointsToMember(pointsToGive, event.getMember(), discordUser);
        }
    }

    private DiscordUser getDiscordUserByMember(Member member) {
        String userId = member.getId();
        Optional<DiscordUser> optionalUserEntity = discordUserRepository.findById(userId);

        if (!optionalUserEntity.isPresent()) {
            DiscordUser newUser = new DiscordUser();
            newUser.setId(userId);
            optionalUserEntity = Optional.of(discordUserRepository.save(newUser));
        }

        return optionalUserEntity.get();
    }

    private void slots(CommandEvent event) {
        Member member = event.getMember();
        DiscordUser discordUser = getDiscordUserByMember(member);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextSlotsTime = discordUser.getLastSlots().plus(SLOTS_TIMEOUT_MINUTES, ChronoUnit.MINUTES);
        if (now.isBefore(nextSlotsTime)) {
            Duration duration = Duration.between(now, nextSlotsTime);

            if (duration.getSeconds() < 60) {
                event.replyError("You must wait " + (duration.getSeconds() + 1) + " seconds before playing slots again");
            } else {
                event.replyError("You must wait " + (duration.toMinutes() + 1) + " minutes before playing slots again");
            }
            return;
        }

        discordUser.setLastSlots(now);

        String one = pickRandom(SPOOKY_SLOTS);
        String two = pickRandom(SPOOKY_SLOTS);
        String three = pickRandom(SPOOKY_SLOTS);

        if (one.equalsIgnoreCase(two) && two.equalsIgnoreCase(three)) {
            givePointsToMember(SLOTS_WIN_POINTS, member, discordUser);
            event.reply(simpleEmbed(SLOT_MACHINE + " WINNER WINNER!! " + SLOT_MACHINE, "%s\n%s%s%s \nYou won " + SLOTS_WIN_POINTS + " points!",
                    member.getAsMention(), one, two, three));
        } else if (one.equalsIgnoreCase(two) || one.equalsIgnoreCase(three) || two.equalsIgnoreCase(three)) {
            givePointsToMember(SLOTS_TWO_OUT_OF_THREE_POINTS, member, discordUser);
            event.reply(simpleEmbed(SLOT_MACHINE + " CLOSE ENOUGH! " + SLOT_MACHINE, "%s\n%s%s%s \nYou got 2 out of 3! You won " + SLOTS_TWO_OUT_OF_THREE_POINTS + " points!",
                    member.getAsMention(), one, two, three));
        } else {
            event.reply(simpleEmbed(SLOT_MACHINE + " Better luck next time! " + SLOT_MACHINE, "%s\n%s%s%s",
                    member.getAsMention(), one, two, three));
            discordUserRepository.save(discordUser);
        }
    }

    private void steal(CommandEvent event) {
        List<Member> mentionedUsers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedUsers) || mentionedUsers.size() > 1) {
            event.replyError("Please only mention one user. Example `!!swampy steal @incogmeato`");
            return;
        }

        Member mentionedMember = mentionedUsers.get(0);
        if (event.getMember().getId().equalsIgnoreCase(mentionedMember.getId())) {
            event.replyError("You can't steal from yourself");
            return;
        }

        if (mentionedMember.getUser().isBot()) {
            event.replyError("You can't steal from bots");
            return;
        }

        DiscordUser mentionedUser = getDiscordUserByMember(mentionedMember);
        if (mentionedUser.getXp() <= 0) {
            event.replyError("You can't steal from them, they don't have any points");
            return;
        }

        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextStealTime = discordUser.getLastSteal().plus(STEAL_TIMEOUT_MINUTES, ChronoUnit.MINUTES);
        if (now.isBefore(nextStealTime)) {
            Duration duration = Duration.between(now, nextStealTime);

            if (duration.getSeconds() < 60) {
                event.replyError("You must wait " + (duration.getSeconds() + 1) + " seconds before stealing again");
            } else {
                event.replyError("You must wait " + (duration.toMinutes() + 1) + " minutes before stealing again");
            }
            return;
        }
        discordUser.setLastSteal(now);
        discordUserRepository.save(discordUser);

        // TODO: Reply with a message, then the person getting stole from has to react to that message in x minutes. if they don't, they lose the points.
        // but if they do, then the stealer, loses the points.
    }

    private void setPointsForMember(long points, Member member) {
        DiscordUser user = getDiscordUserByMember(member);
        user.setXp(points);
        user = discordUserRepository.save(user);
        assignRolesIfNeeded(member, user);
    }

    private void showRank(CommandEvent event) {
        Member member = event.getMember();
        if (CollectionUtils.isNotEmpty(event.getMessage().getMentionedUsers())) {
            member = event.getMessage().getMentionedMembers().get(0);
        }

        if (member.getUser().isBot() || hasRole(member, Constants.NEWBIE_ROLE)) {
            event.reply(simpleEmbed("Your Rank", "You can't see rank because it appears you are a newbie or a bot"));
            return;
        }

        DiscordUser user = getDiscordUserByMember(member);
        Role role = assignRolesIfNeeded(member, user);

        Rank[] allRanks = Rank.values();
        Rank rank = Rank.byXp(user.getXp());
        Rank nextRank = (rank.ordinal() > allRanks.length - 1) ? rank : allRanks[rank.ordinal() + 1];

        MessageEmbed messageEmbed = new EmbedBuilder()
                .setImage(rank.getRankUpImage())
                .setTitle("Level " + rank.getLevel() + ": " + rank.getRoleName())
                .setColor(role.getColor())
                .setDescription(rank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", rank.getRoleName()) +
                        "\n\nYou have " + NumberFormat.getIntegerInstance().format(user.getXp()) + " total points.\n\n" +
                        "The next level is level " +
                        (rank == nextRank
                                ? " LOL NVM YOU'RE THE TOP LEVEL."
                                : nextRank.getLevel() + ": " + nextRank.getRoleName() + ".") +
                        "\n You have " + NumberFormat.getIntegerInstance().format((nextRank.getLevel() * Rank.LVL_MULTIPLIER) - user.getXp()) + " points to go." +
                        "\n\nBest of Luck in the Swampys!")
                .build();

        event.reply(messageEmbed);
    }

    private void leaderboard(CommandEvent event) {
        String[] split = event.getArgs().split("\\s+");
        if (split.length != 2) {
            event.replyError("Badly formatted command. Example `!!swampy leaderboard bastard` or `!!swampy leaderboard chaos`");
            return;
        }

        List<DiscordUser> swampyUsers = discordUserRepository.findAll();

        AtomicInteger place = new AtomicInteger(0);
        StringBuilder description = new StringBuilder();
        swampyUsers.stream().filter(u ->
            hasRole(event.getGuild().retrieveMemberById(u.getId()).complete(),
                    StringUtils.containsIgnoreCase(split[1], "bastard") ? Constants.EIGHTEEN_PLUS_ROLE : Constants.CHAOS_CHILDREN_ROLE)
        ).sorted((u1, u2) -> Long.compare(u2.getXp(), u1.getXp())).limit(10).forEachOrdered(swampyUser -> {
            description.append(LEADERBOARD_MEDALS[place.getAndIncrement()])
                    .append(": <@!")
                    .append(swampyUser.getId())
                    .append("> - ")
                    .append(NumberFormat.getIntegerInstance().format(swampyUser.getXp()))
                    .append('\n');
        });

        MessageEmbed messageEmbed = new EmbedBuilder()
                .setTitle(StringUtils.containsIgnoreCase(split[1], "bastard") ? "Bastard Leaderboard" : "Chaos Leaderboard")
                .setDescription(description.toString())
                .build();

        event.reply(messageEmbed);
    }

    private MessageEmbed simpleEmbed(String title, String format, Object... args) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(String.format(format, args))
                .build();
    }

    private void upvote(CommandEvent event) {
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedMembers) || mentionedMembers.size() > 1) {
            event.replyError("Please mention one user to upvote. Example `!!swampy up @incogmeato`");
            return;
        }

        if (mentionedMembers.get(0).getUser().isBot() || hasRole(mentionedMembers.get(0), Constants.NEWBIE_ROLE)) {
            event.replyError(mentionedMembers.get(0).getEffectiveName() + " is not participating in the swampys");
            return;
        }

        if (event.getMember().getId().equalsIgnoreCase(mentionedMembers.get(0).getId())) {
            event.replyError("You can't upvote yourself");
            return;
        }

        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextVoteTime = discordUser.getLastVote().plus(UPVOTE_TIMEOUT_MINUTES, ChronoUnit.MINUTES);
        if (now.isBefore(nextVoteTime)) {
            Duration duration = Duration.between(now, nextVoteTime);

            if (duration.getSeconds() < 60) {
                event.replyError("You must wait " + (duration.getSeconds() + 1) + " seconds before up/down-voting again");
            } else {
                event.replyError("You must wait " + (duration.toMinutes() + 1) + " minutes before up/down-voting again");
            }
            return;
        }
        discordUser.setLastVote(now);
        discordUserRepository.save(discordUser);

        givePointsToMember(UPVOTE_POINTS_TO_UPVOTEE, mentionedMembers.get(0));
        givePointsToMember(UPVOTE_POINTS_TO_UPVOTER, event.getMember());

        event.replySuccess("Successfully upvoted " + mentionedMembers.get(0).getEffectiveName());
    }

    private void downvote(CommandEvent event) {
        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedMembers) || mentionedMembers.size() > 1) {
            event.replyError("Please mention one user to downvote. Example `!!swampy down @incogmeato`");
            return;
        }

        if (mentionedMembers.get(0).getUser().isBot() || hasRole(mentionedMembers.get(0), Constants.NEWBIE_ROLE)) {
            event.replyError(mentionedMembers.get(0).getEffectiveName() + " is not participating in the swampys");
            return;
        }

        if (event.getMember().getId().equalsIgnoreCase(mentionedMembers.get(0).getId())) {
            event.replyError("You can't downvote yourself");
            return;
        }

        DiscordUser discordUser = getDiscordUserByMember(event.getMember());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextVoteTime = discordUser.getLastVote().plus(DOWNVOTE_TIMEOUT_MINUTES, ChronoUnit.MINUTES);
        if (now.isBefore(nextVoteTime)) {
            Duration duration = Duration.between(now, nextVoteTime);

            if (duration.getSeconds() < 60) {
                event.replyError("You must wait " + (duration.getSeconds() + 1) + " seconds before up/down-voting again");
            } else {
                event.replyError("You must wait " + (duration.toMinutes() + 1) + " minutes before up/down-voting again");
            }
            return;
        }
        discordUser.setLastVote(now);
        discordUserRepository.save(discordUser);

        takePointsFromMember(DOWNVOTE_POINTS_FROM_DOWNVOTEE, mentionedMembers.get(0));
        givePointsToMember(DOWNVOTE_POINTS_TO_DOWNVOTER, event.getMember());

        event.replySuccess("Successfully downvoted " + mentionedMembers.get(0).getEffectiveName());
    }

    private void give(CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        String[] split = event.getArgs().split("\\s+");
        if (split.length != 3) {
            event.replyError("Badly formatted command. Example `!!swampy give 100 @incogmeato`");
            return;
        }

        long pointsToGive;
        try {
            pointsToGive = Long.parseLong(split[1]);
        } catch (NumberFormatException e) {
            event.replyError("Failed to parse the number you provided.");
            return;
        }

        if (pointsToGive < 0) {
            event.replyError("Please provide a positive number");
            return;
        }

        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedMembers) || mentionedMembers.size() > 1) {
            event.replyError("Please specify only one user. Example `!!swampy give 100 @incogmeato`");
            return;
        }

        if (mentionedMembers.get(0).getUser().isBot() || hasRole(mentionedMembers.get(0), Constants.NEWBIE_ROLE)) {
            event.replyError(mentionedMembers.get(0).getEffectiveName() + " is not participating in the swampys");
            return;
        }

        givePointsToMember(pointsToGive, mentionedMembers.get(0));

        event.replyFormatted("Added %s xp to %s", NumberFormat.getIntegerInstance().format(pointsToGive), mentionedMembers.get(0).getEffectiveName());
    }

    private void take(CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        String[] split = event.getArgs().split("\\s+");
        if (split.length != 3) {
            event.replyError("Badly formatted command. Example `!!swampy take 100 @incogmeato`");
            return;
        }

        long pointsToTake;
        try {
            pointsToTake = Long.parseLong(split[1]);
        } catch (NumberFormatException e) {
            event.replyError("Failed to parse the number you provided.");
            return;
        }

        if (pointsToTake < 0) {
            event.replyError("Please provide a positive number");
            return;
        }

        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedMembers) || mentionedMembers.size() > 1) {
            event.replyError("Please specify only one user. Example `!!swampy take 100 @incogmeato`");
            return;
        }

        if (mentionedMembers.get(0).getUser().isBot() || hasRole(mentionedMembers.get(0), Constants.NEWBIE_ROLE)) {
            event.replyError(mentionedMembers.get(0).getEffectiveName() + " is not participating in the swampy games");
            return;
        }

        takePointsFromMember(pointsToTake, mentionedMembers.get(0));

        event.replyFormatted("Removed %s xp from %s", NumberFormat.getIntegerInstance().format(pointsToTake), mentionedMembers.get(0).getEffectiveName());
    }

    private void set(CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        String[] split = event.getArgs().split("\\s+");
        if (split.length != 3) {
            event.replyError("Badly formatted command. Example `!!swampy set 100 @incogmeato`");
            return;
        }

        long pointsToSet;
        try {
            pointsToSet = Long.parseLong(split[1]);
        } catch (NumberFormatException e) {
            event.replyError("Failed to parse the number you provided.");
            return;
        }

        if (pointsToSet < 0) {
            event.replyError("Please provide a positive number");
            return;
        }

        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();

        if (CollectionUtils.isEmpty(mentionedMembers) || mentionedMembers.size() > 1) {
            event.replyError("Please specify only one user. Example `!!swampy set 100 @incogmeato`");
            return;
        }

        if (mentionedMembers.get(0).getUser().isBot() || hasRole(mentionedMembers.get(0), Constants.NEWBIE_ROLE)) {
            event.replyError(mentionedMembers.get(0).getEffectiveName() + " is not participating in the swampys");
            return;
        }

        setPointsForMember(pointsToSet, mentionedMembers.get(0));

        event.replyFormatted("Set xp to %s for %s", NumberFormat.getIntegerInstance().format(pointsToSet), mentionedMembers.get(0).getEffectiveName());
    }

    private void reset(CommandEvent event) {
        if (!hasRole(event.getMember(), Constants.ADMIN_ROLE)) {
            event.replyError("You do not have permission to use this command");
            return;
        }

        for (DiscordUser discordUser : discordUserRepository.findAll()) {
            discordUser.setXp(0);
            discordUser = discordUserRepository.save(discordUser);

            try {
                final DiscordUser finalDiscordUser = discordUser;
                event.getGuild().retrieveMemberById(discordUser.getId()).submit().whenComplete((member, err) -> {
                    if (member != null) {
                        assignRolesIfNeeded(member, finalDiscordUser);
                    } else {
                        event.replyError("Failed to reset roles for user with discord id: <@!" + finalDiscordUser.getId() + '>');
                    }
                });
            } catch (Exception e) {
                logger.error("Failed to reset roles for user with [id={}]", discordUser.getId(), e);
                event.replyError("Failed to reset roles for user with discord id: <@!" + discordUser.getId() + '>');
            }
        }

        event.replySuccess("Reset the Swampys");
    }

    private Role assignRolesIfNeeded(Member member, DiscordUser user) {
        if (member.getUser().isBot() || hasRole(member, Constants.NEWBIE_ROLE)) {
            return null;
        }

        Rank newRank = Rank.byXp(user.getXp());
        Role newRole = member.getGuild().getRolesByName(newRank.getRoleName(), true).get(0);

        if (!hasRole(member, newRank)) {
            Set<String> allRoleNames = Rank.getAllRoleNames();
            Guild guild = member.getGuild();

            member.getRoles().stream().filter(r -> allRoleNames.contains(r.getName())).forEach(roleToRemove -> {
                guild.removeRoleFromMember(member, roleToRemove).complete();
            });

            guild.addRoleToMember(member, newRole).complete();

            if (newRank != Rank.CINNAMON_ROLL) {
                TextChannel announcementsChannel = member.getGuild().getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

                MessageEmbed messageEmbed = new EmbedBuilder()
                        .setImage(newRank.getRankUpImage())
                        .setTitle("Level Change!")
                        .setColor(newRole.getColor())
                        .setDescription(newRank.getRankUpMessage().replace("<name>", member.getAsMention()).replace("<rolename>", newRank.getRoleName()))
                        .build();

                announcementsChannel.sendMessage(messageEmbed).queue();
            }
        }

        return newRole;
    }

    private static boolean hasRole(Member member, Rank rank) {
        return hasRole(member, rank.getRoleName());
    }

    private static boolean hasRole(Member member, String role) {
        return member.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(role));
    }

    private static <T> T pickRandom(Collection<T> collection) {
        int index = ThreadLocalRandom.current().nextInt(collection.size());
        Iterator<T> iterator = collection.iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

}
