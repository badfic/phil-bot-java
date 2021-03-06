package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.service.MinuteTickable;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SwiperTickable extends NonCommandSwampy implements MinuteTickable {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void run() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();
        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

        String swiperAwaiting = swampyGamesConfig.getSwiperAwaiting();
        int swiperPoints = swampyGamesConfig.getSwiperPoints();
        String swiperSavior = swampyGamesConfig.getSwiperSavior();
        String noSwipingPhrase = swampyGamesConfig.getNoSwipingPhrase();
        LocalDateTime swiperExpiration = swampyGamesConfig.getSwiperExpiration();

        if (swiperAwaiting != null && swiperExpiration.isBefore(LocalDateTime.now())) {
            swampyGamesConfig.setNoSwipingPhrase(null);
            swampyGamesConfig.setSwiperAwaiting(null);
            swampyGamesConfig.setSwiperSavior(null);
            swampyGamesConfig.setSwiperExpiration(null);
            swampyGamesConfigRepository.save(swampyGamesConfig);

            Swiper.TheSwiper theSwiper = Swiper.SWIPERS.get(noSwipingPhrase);
            Optional<DiscordUser> victim = discordUserRepository.findById(swiperAwaiting);

            MessageEmbed message = Constants.simpleEmbed(theSwiper.getNoSwipingPhrase(),
                    "Congratulations, <@!" + philJda.getSelfUser().getId() + "> is a moron so nobody loses any points",
                    theSwiper.getSwiperLostImage(), null, null, philJda.getSelfUser().getEffectiveAvatarUrl());

            if (victim.isPresent()) {
                if (swiperSavior != null) {
                    try {
                        Optional<DiscordUser> savior = discordUserRepository.findById(swiperSavior);

                        String image = savior.isPresent() && savior.get().getId().equalsIgnoreCase(victim.get().getId())
                                ? Swiper.SPIDERMAN_PNG
                                : theSwiper.getSwiperLostImage();

                        message = Constants.simpleEmbed(theSwiper.getNoSwipingPhrase(),
                                "Congratulations, <@!" + (savior.isPresent() ? savior.get().getId() : "somebody")
                                        + "> scared them away from <@!" + victim.get().getId() + ">",
                                image);

                        if (savior.isPresent()) {
                            savior.get().setSwiperParticipations(savior.get().getSwiperParticipations() + 1);
                            discordUserRepository.save(savior.get());
                            Member saviorMember = philJda.getGuilds().get(0).getMemberById(savior.get().getId());
                            if (saviorMember != null) {
                                givePointsToMember(700, saviorMember, PointsStat.SWIPER);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Exception with swiper savior branch", e);
                        honeybadgerReporter.reportError(e, "Exception during swiper savior logic");
                    }
                } else {
                    try {
                        Member memberById = philJda.getGuilds().get(0).getMemberById(victim.get().getId());

                        if (memberById != null) {
                            takePointsFromMember(swiperPoints, memberById, PointsStat.SWIPER);
                            message = Constants.simpleEmbed(theSwiper.getSwiperWonPhrase(),
                                    "You didn't save <@!" + victim.get().getId() + "> in time, they lost " + swiperPoints + " points",
                                    theSwiper.getSwiperWonImage());
                        }
                    } catch (Exception e) {
                        logger.error("Exception looking up swiper victim [id={}] after they were not saved", victim.get().getId(), e);
                        honeybadgerReporter.reportError(e, "Exception looking up swiper victim after they were not saved: " + victim.get().getId());
                    }
                }
            }

            swampysChannel.sendMessage(message).queue();
        }
    }

}
