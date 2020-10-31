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

    private static final long SWIPER_POINTS_TO_STEAL = 1_500;

    private static final String SWIPER_SPOTTED = "https://cdn.discordapp.com/attachments/323666308107599872/761495531498373120/swiper-spotted.jpg";
    private static final String SWIPER_WON = "https://cdn.discordapp.com/attachments/761398315119280158/765049616412180500/stephen-molyneaux-dora-swiper-1001.png";
    private static final String NO_SWIPING = "https://cdn.discordapp.com/attachments/707453916882665552/757776008639283321/unknown.png";
    private static final String SNART_SPOTTED = "https://cdn.discordapp.com/attachments/323666308107599872/758910821950029824/snart_rory.png";
    private static final String SNART_WON = "https://cdn.discordapp.com/attachments/323666308107599872/758911981176094801/snart_rory_mischief_managed.png";
    private static final String NO_SNART = "https://cdn.discordapp.com/attachments/323666308107599872/758911984925802516/snart_rory_you_cant_steal_here.png";
    private static final String SPIDERMAN_PNG = "https://cdn.discordapp.com/attachments/707453916882665552/769837667617079316/spiderman.png";

    public Swiper() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "swiper";
    }

    @Override
    public void execute(CommandEvent event) {
        event.replyError("Manually triggering swiper has been disabled.");
    }

    @Scheduled(cron = "0 20,35 0,2,4,6,8,10,12,14,16,18,20,22 * * ?", zone = "GMT")
    public void swiper() {
        doSwiper();
    }

    private void doSwiper() {
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

                    String image;
                    if (savior.isPresent() && savior.get().getId().equalsIgnoreCase(victim.get().getId())) {
                        image = SPIDERMAN_PNG;
                    } else if (StringUtils.containsIgnoreCase(noSwipingPhrase, "swiper")) {
                        image = NO_SWIPING;
                    } else {
                        image = NO_SNART;
                    }

                    message = new EmbedBuilder()
                            .setTitle(noSwipingPhrase)
                            .setDescription("Congratulations, <@!" + (savior.isPresent() ? savior.get().getId() : "somebody")
                                    + "> scared them away from <@!" + victim.get().getId() + ">")
                            .setColor(Constants.HALOWEEN_ORANGE)
                            .setImage(image)
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
        List<DiscordUser> allUsers = discordUserRepository.findAll();
        Collections.shuffle(allUsers);

        for (DiscordUser winningUser : allUsers) {
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

        if (member == null) {
            logger.info("Swiper did not find any eligible victim");
            return;
        }

        boolean swiper = ThreadLocalRandom.current().nextInt() % 2 == 0;
        swampyGamesConfig.setSwiperAwaiting(member.getId());
        swampyGamesConfig.setNoSwipingPhrase(swiper ? "Swiper No Swiping" : "Snarter No Snarting");
        swampyGamesConfig = swampyGamesConfigRepository.save(swampyGamesConfig);

        String description = "They're trying to steal from <@!" + member.getId() + ">\nType '" + swampyGamesConfig.getNoSwipingPhrase()
                + "' in this channel within 15 minutes to stop them!";

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
