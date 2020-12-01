package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.config.PhilMarker;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.google.common.collect.ImmutableMap;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Swiper extends BaseSwampy implements PhilMarker {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // TODO: Investigate putting these in SwampyGamesConfig table
    private static final Map<String, TheSwiper> SWIPERS = ImmutableMap.<String, TheSwiper>builder()
            .put("Swiper No Swiping", new TheSwiper(
                    "Swiper No Swiping",
                    "Swiper Was Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/707453916882665552/782824147780632576/maxresdefault.png",
                    "https://cdn.discordapp.com/attachments/707453916882665552/782823673526616114/swiper.png",
                    "Swiper Escaped!",
                    "https://cdn.discordapp.com/attachments/707453916882665552/782824264989802526/1383968722_4.png"
            ))
            .put("Snarter No Snarting", new TheSwiper(
                    "Snarter No Snarting",
                    "Rory and Snart Were Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/323666308107599872/758910821950029824/snart_rory.png",
                    "https://cdn.discordapp.com/attachments/323666308107599872/758911984925802516/snart_rory_you_cant_steal_here.png",
                    "Rory and Snart Escaped!",
                    "https://cdn.discordapp.com/attachments/323666308107599872/758911981176094801/snart_rory_mischief_managed.png"
            ))
            .put("Squidward No Squidding", new TheSwiper(
                    "Squidward No Squidding",
                    "Squidward Was Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/707453916882665552/779842927655780372/99784025-0c213880-2ad0-11eb-832e-a07fd430914a.png",
                    "https://cdn.discordapp.com/attachments/707453916882665552/779843093448097852/99784075-1ba08180-2ad0-11eb-91a4-8f72a43df4db.png",
                    "Squidward Escaped!",
                    "https://cdn.discordapp.com/attachments/707453916882665552/779843213808238642/99784155-3d016d80-2ad0-11eb-862e-bccfec33eb9f.png"
            ))
            .put("Simper No Simping", new TheSwiper(
                    "Simper No Simping",
                    "Simps Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/761398315119280158/772926342945570836/gobblesimp.png",
                    "https://cdn.discordapp.com/attachments/707453916882665552/779844003855204393/99784554-d6308400-2ad0-11eb-8645-ae51bc4785df.png",
                    "The Simps Escaped!",
                    "https://cdn.discordapp.com/attachments/707453916882665552/779844165433163776/99784902-50610880-2ad1-11eb-937c-6e768aa4f5db.png"
            ))
            .put("Shrimper No Shrimping", new TheSwiper(
                    "Shrimper No Shrimping",
                    "Shrimp Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/323666308107599872/782162173395861504/83585dee533fb9cb2e3e4e28e675a911.png",
                    "https://cdn.discordapp.com/attachments/323666308107599872/782164940042928158/18aa30d62c66eb2ca17b2817c3de5a2b.png",
                    "The Shrimp Escaped!",
                    "https://cdn.discordapp.com/attachments/323666308107599872/782166177345961994/ssrcoclassic_teewomensfafafaca443f4786front_altsquare_product600x600.png"
            ))
            .put("Schuester No Schuesting", new TheSwiper(
                    "Schuester No Schuesting",
                    "Schuester Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/741025219346563073/782100623352397845/image0.png",
                    "https://cdn.discordapp.com/attachments/741025219346563073/782079649021689886/image0.png",
                    "Schuester Escaped!",
                    "https://cdn.discordapp.com/attachments/741025219346563073/782406865601822750/image0.png"
            ))
            .build();

    private static final String SPIDERMAN_PNG = "https://cdn.discordapp.com/attachments/707453916882665552/769837667617079316/spiderman.png";

    public Swiper() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "swiper";
        help = "Manually triggering swiper has been disabled";
    }

    @Override
    public void execute(CommandEvent event) {
        if (baseConfig.ownerId.equalsIgnoreCase(event.getAuthor().getId())) {
            doSwiper();
        } else {
            event.replyError("Manually triggering swiper has been disabled.");
        }
    }

    @Scheduled(cron = "0 20,35 0,4,6,8,10,12,14,16,18,20,22 * * ?", zone = "GMT")
    public void swiper() {
        doSwiper();
    }

    private void doSwiper() {
        Optional<SwampyGamesConfig> optionalConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID);
        if (!optionalConfig.isPresent()) {
            return;
        }
        SwampyGamesConfig swampyGamesConfig = optionalConfig.get();

        TextChannel[] channels = new TextChannel[] {
                philJda.getTextChannelsByName(Constants.DRY_BASTARDS_CHANNEL, false).get(0),
                philJda.getTextChannelsByName(Constants.DRY_CINNAMON_CHANNEL, false).get(0),
                philJda.getTextChannelsByName(Constants.SWAMPY_BASTARD_CHANNEL, false).get(0),
                philJda.getTextChannelsByName(Constants.SWAMPY_CINNAMON_CHANNEL, false).get(0)
        };

        if (swampyGamesConfig.getSwiperAwaiting() != null) {
            TheSwiper theSwiper = SWIPERS.get(swampyGamesConfig.getNoSwipingPhrase());

            swampyGamesConfig.setNoSwipingPhrase(null);

            Optional<DiscordUser> victim = discordUserRepository.findById(swampyGamesConfig.getSwiperAwaiting());
            swampyGamesConfig.setSwiperAwaiting(null);

            MessageEmbed message = Constants.simpleEmbed(theSwiper.getNoSwipingPhrase(),
                    "Congratulations, <@!" + philJda.getSelfUser().getId() + "> is a moron so nobody loses any points",
                    theSwiper.getSwiperLostImage());

            if (victim.isPresent()) {
                if (swampyGamesConfig.getSwiperSavior() != null) {
                    try {
                        Optional<DiscordUser> savior = discordUserRepository.findById(swampyGamesConfig.getSwiperSavior());
                        swampyGamesConfig.setSwiperSavior(null);

                        String image = savior.isPresent() && savior.get().getId().equalsIgnoreCase(victim.get().getId())
                                ? SPIDERMAN_PNG
                                : theSwiper.getSwiperLostImage();

                        message = Constants.simpleEmbed(theSwiper.getNoSwipingPhrase(),
                                "Congratulations, <@!" + (savior.isPresent() ? savior.get().getId() : "somebody")
                                        + "> scared them away from <@!" + victim.get().getId() + ">",
                                image);

                        if (savior.isPresent()) {
                            savior.get().setSwiperParticipations(savior.get().getSwiperParticipations() + 1);
                            discordUserRepository.save(savior.get());
                        }
                    } catch (Exception e) {
                        logger.error("Exception with swiper savior branch", e);
                        honeybadgerReporter.reportError(e, "Exception during swiper savior logic");
                    }
                } else {
                    try {
                        Member memberById = philJda.getGuilds().get(0).getMemberById(victim.get().getId());

                        if (memberById != null) {
                            takePointsFromMember(swampyGamesConfig.getSwiperPoints(), memberById);
                            message = Constants.simpleEmbed(theSwiper.getSwiperWonPhrase(),
                                    "You didn't save <@!" + victim.get().getId() + "> in time, they lost " + swampyGamesConfig.getSwiperPoints() + " points",
                                    theSwiper.getSwiperWonImage());
                        }
                    } catch (Exception e) {
                        logger.error("Exception looking up swiper victim [id={}] after they were not saved", victim.get().getId(), e);
                        honeybadgerReporter.reportError(e, "Exception looking up swiper victim after they were not saved: " + victim.get().getId());
                    }
                }
            }

            swampyGamesConfigRepository.save(swampyGamesConfig);

            for (TextChannel channel : channels) {
                channel.sendMessage(message).queue();
            }

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
                        && winningUser.getXp() > swampyGamesConfig.getSwiperPoints()
                        && winningUser.getUpdateTime().isAfter(LocalDateTime.now().minusHours(22))) {
                    member = memberById;
                }
            } catch (Exception ignored) {}
        }

        if (member == null) {
            logger.info("Swiper did not find any eligible victim");
            return;
        }

        TheSwiper theSwiper = Constants.pickRandom(SWIPERS.values());

        swampyGamesConfig.setSwiperAwaiting(member.getId());
        swampyGamesConfig.setNoSwipingPhrase(theSwiper.getNoSwipingPhrase());
        swampyGamesConfig = swampyGamesConfigRepository.save(swampyGamesConfig);

        String description = "They're trying to steal from <@!" + member.getId() + ">\nType '" + swampyGamesConfig.getNoSwipingPhrase()
                + "' in this channel within 15 minutes to stop them!";

        MessageEmbed message = Constants.simpleEmbed(theSwiper.getSpottedPhrase(), description, theSwiper.getSpottedImage());

        for (TextChannel channel : channels) {
            channel.sendMessage(message).queue();
        }
    }

    private static class TheSwiper {
        private final String noSwipingPhrase;
        private final String spottedPhrase;
        private final String spottedImage;
        private final String swiperLostImage;
        private final String swiperWonPhrase;
        private final String swiperWonImage;

        public TheSwiper(String noSwipingPhrase, String spottedPhrase, String spottedImage, String swiperLostImage,
                         String swiperWonPhrase, String swiperWonImage) {
            this.noSwipingPhrase = noSwipingPhrase;
            this.spottedPhrase = spottedPhrase;
            this.spottedImage = spottedImage;
            this.swiperLostImage = swiperLostImage;
            this.swiperWonPhrase = swiperWonPhrase;
            this.swiperWonImage = swiperWonImage;
        }

        public String getNoSwipingPhrase() {
            return noSwipingPhrase;
        }

        public String getSpottedPhrase() {
            return spottedPhrase;
        }

        public String getSpottedImage() {
            return spottedImage;
        }

        public String getSwiperLostImage() {
            return swiperLostImage;
        }

        public String getSwiperWonPhrase() {
            return swiperWonPhrase;
        }

        public String getSwiperWonImage() {
            return swiperWonImage;
        }
    }

}
