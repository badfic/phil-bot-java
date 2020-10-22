package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Swiper extends BaseSwampy implements PhilMarker {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final long SWIPER_POINTS_TO_STEAL = 1_500;

    public static final String SWIPER_SPOTTED = "https://cdn.discordapp.com/attachments/323666308107599872/761495531498373120/swiper-spotted.jpg";
    public static final String SWIPER_WON = "https://cdn.discordapp.com/attachments/761398315119280158/765049616412180500/stephen-molyneaux-dora-swiper-1001.png";
    public static final String NO_SWIPING = "https://cdn.discordapp.com/attachments/707453916882665552/757776008639283321/unknown.png";
    public static final String SNART_SPOTTED = "https://cdn.discordapp.com/attachments/323666308107599872/758910821950029824/snart_rory.png";
    public static final String SNART_WON = "https://cdn.discordapp.com/attachments/323666308107599872/758911981176094801/snart_rory_mischief_managed.png";
    public static final String NO_SNART = "https://cdn.discordapp.com/attachments/323666308107599872/758911984925802516/snart_rory_you_cant_steal_here.png";

    public Swiper() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "swiper";
    }

    @Override
    public void execute(CommandEvent event) {
        event.replyError("Manually triggering swiper has been disabled for now.");
    }

    @Scheduled(cron = "0 20,35 0,2,4,6,8,10,12,14,16,18,20,22 * * ?", zone = "GMT")
    public void swiper() {
        doSwiper(null);
    }

    private void doSwiper(String userId) {
        Optional<SwampyGamesConfig> optionalConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);
        if (!optionalConfig.isPresent()) {
            return;
        }
        SwampyGamesConfig swampyGamesConfig = optionalConfig.get();

        if (swampyGamesConfig.getSwiperAwaiting() != null) {
            String noSwipingPhrase = swampyGamesConfig.getNoSwipingPhrase();
            swampyGamesConfig.setNoSwipingPhrase(null);

            Optional<DiscordUser> victim = discordUserRepository.findById(swampyGamesConfig.getSwiperAwaiting());
            swampyGamesConfig.setSwiperAwaiting(null);

            MessageEmbed message = new EmbedBuilder()
                    .setTitle(noSwipingPhrase)
                    .setDescription("Congratulations, <@!" + philJda.getSelfUser().getId() + "> is a moron so nobody loses any points")
                    .setColor(Constants.HALOWEEN_ORANGE)
                    .setImage(NO_SWIPING)
                    .build();

            if (victim.isPresent()) {
                if (swampyGamesConfig.getSwiperSavior() != null) {
                    Optional<DiscordUser> savior = discordUserRepository.findById(swampyGamesConfig.getSwiperSavior());
                    swampyGamesConfig.setSwiperSavior(null);

                    message = new EmbedBuilder()
                            .setTitle(noSwipingPhrase)
                            .setDescription("Congratulations, <@!" + (savior.isPresent() ? savior.get().getId() : "somebody") + "> scared them away from <@!" + victim.get().getId() + ">")
                            .setColor(Constants.HALOWEEN_ORANGE)
                            .setImage(StringUtils.containsIgnoreCase(noSwipingPhrase, "swiper") ? NO_SWIPING : NO_SNART)
                            .build();
                } else {
                    try {
                        Member memberById = philJda.getGuilds().get(0).getMemberById(victim.get().getId());

                        if (memberById != null) {
                            takePointsFromMember(SWIPER_POINTS_TO_STEAL, memberById);
                            message = new EmbedBuilder()
                                    .setTitle(StringUtils.containsIgnoreCase(noSwipingPhrase, "swiper") ? "Swiper Escaped!" : "Rory and Snart Escaped!")
                                    .setDescription("You didn't save <@!" + victim.get().getId() + "> in time, they lost " + SWIPER_POINTS_TO_STEAL + " points")
                                    .setColor(Constants.HALOWEEN_ORANGE)
                                    .setImage(StringUtils.containsIgnoreCase(noSwipingPhrase, "swiper") ? SWIPER_WON : SNART_WON)
                                    .build();
                        }
                    } catch (Exception e) {
                        logger.error("Exception looking up swiper victim [id={}] after they were not saved", victim.get().getId(), e);
                    }
                }
            }

            swampyGamesConfigRepository.save(swampyGamesConfig);
            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
            return;
        }

        Member member = null;
        if (userId != null) {
            try {
                Member memberById = philJda.getGuilds().get(0).getMemberById(userId);
                if (memberById != null && !memberById.getUser().isBot()) {
                    member = memberById;
                }
            } catch (Exception ignored) {}
        } else {
            List<DiscordUser> allUsers = discordUserRepository.findAll();
            Collections.shuffle(allUsers);

            long startTime = System.currentTimeMillis();

            while (member == null && System.currentTimeMillis() - startTime < TimeUnit.SECONDS.toMillis(30)) {
                DiscordUser winningUser = pickRandom(allUsers);

                try {
                    Member memberById = philJda.getGuilds().get(0).getMemberById(winningUser.getId());
                    if (memberById != null
                            && !memberById.getUser().isBot()
                            && winningUser.getXp() > SWIPER_POINTS_TO_STEAL
                            && winningUser.getUpdateTime().isAfter(LocalDateTime.now().minusHours(24))) {
                        member = memberById;
                    }
                } catch (Exception ignored) {}
            }
        }

        if (member == null) {
            MessageEmbed message = new EmbedBuilder()
                    .setTitle("Swiper Was Spotted Nearby")
                    .setDescription("Swiper was spotted nearby, be on the lookout for him")
                    .setColor(Constants.HALOWEEN_ORANGE)
                    .setImage(NO_SWIPING)
                    .build();

            philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                    .get(0)
                    .sendMessage(message)
                    .queue();
            return;
        }

        boolean swiper = ThreadLocalRandom.current().nextInt() % 2 == 0;
        swampyGamesConfig.setSwiperAwaiting(member.getId());
        swampyGamesConfig.setNoSwipingPhrase(swiper ? "Swiper No Swiping" : "Snarter No Snarting");
        swampyGamesConfig = swampyGamesConfigRepository.save(swampyGamesConfig);

        String description = "They're trying to steal from <@!" + member.getId() + ">\nType '" + swampyGamesConfig.getNoSwipingPhrase()
                + "' in this channel to stop them!";

        MessageEmbed message = new EmbedBuilder()
                .setTitle(swiper ? "Swiper Was Spotted Nearby" : "Rory and Snart Were Spotted Nearby")
                .setDescription(description)
                .setColor(Constants.HALOWEEN_ORANGE)
                .setImage(swiper ? SWIPER_SPOTTED : SNART_SPOTTED)
                .build();

        philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false)
                .get(0)
                .sendMessage(message)
                .queue();
    }

}
