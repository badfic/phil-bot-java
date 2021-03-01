package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.data.DiscordUser;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.google.common.collect.ImmutableMap;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Swiper extends BaseSwampy {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // TODO: Investigate putting these in SwampyGamesConfig table
    public static final Map<String, TheSwiper> SWIPERS = ImmutableMap.<String, TheSwiper>builder()
            .put("Swiper No Swiping", new TheSwiper(
                    "Swiper No Swiping",
                    "Swiper Was Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/707453916882665552/798774215813627944/419DS5921XL.png",
                    "https://cdn.discordapp.com/attachments/707453916882665552/798774612036026388/d95dfb7216c107d4b1411aafbd7c8abf.png",
                    "Swiper Escaped!",
                    "https://cdn.discordapp.com/attachments/707453916882665552/798774471280427028/419rWJgCx4L.png"
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
                    "Squidward Squidded Away With Yo Points!",
                    "https://cdn.discordapp.com/attachments/707453916882665552/779843213808238642/99784155-3d016d80-2ad0-11eb-862e-bccfec33eb9f.png"
            ))
            .put("Simper No Simping", new TheSwiper(
                    "Simper No Simping",
                    "Simps Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/761398315119280158/772926342945570836/gobblesimp.png",
                    "https://cdn.discordapp.com/attachments/707453916882665552/779844003855204393/99784554-d6308400-2ad0-11eb-8645-ae51bc4785df.png",
                    "The Simps Prospered!",
                    "https://cdn.discordapp.com/attachments/707453916882665552/779844165433163776/99784902-50610880-2ad1-11eb-937c-6e768aa4f5db.png"
            ))
            .put("Shrimper No Shrimping", new TheSwiper(
                    "Shrimper No Shrimping",
                    "Shrimp Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/323666308107599872/782162173395861504/83585dee533fb9cb2e3e4e28e675a911.png",
                    "https://cdn.discordapp.com/attachments/323666308107599872/782164940042928158/18aa30d62c66eb2ca17b2817c3de5a2b.png",
                    "The Shrimp Shrimped Away!",
                    "https://cdn.discordapp.com/attachments/323666308107599872/782166177345961994/ssrcoclassic_teewomensfafafaca443f4786front_altsquare_product600x600.png"
            ))
            .put("Schuester No Schuesting", new TheSwiper(
                    "Schuester No Schuesting",
                    "Schuester Spotted Nearby",
                    "https://cdn.discordapp.com/attachments/741025219346563073/782100623352397845/image0.png",
                    "https://cdn.discordapp.com/attachments/741025219346563073/782079649021689886/image0.png",
                    "The Grinch Stole Your Soul!",
                    "https://cdn.discordapp.com/attachments/741025219346563073/782406865601822750/image0.png"
            ))
            .put("Chooster No Choosting", new TheSwiper(
                    "Chooster No Choosting",
                    "Thomas The Tank Engine Spotted",
                    "https://cdn.discordapp.com/attachments/707453916882665552/783971752857436170/450.png",
                    "https://cdn.discordapp.com/attachments/707453916882665552/783971521113882674/Thomas.png",
                    "Sweet Sweet Murder",
                    "https://cdn.discordapp.com/attachments/707453916882665552/783971471486615552/254c4699bb69c12a21714605beebd3f4.png"
            ))
            .put("Panic No Discoing", new TheSwiper(
                    "Panic No Discoing",
                    "Brendon Urie Spotted",
                    "https://cdn.discordapp.com/attachments/707453916882665552/802675259303526490/iu.png",
                    "https://cdn.discordapp.com/attachments/707453916882665552/802675392665878528/iu.png",
                    "Panic! At The Crisco",
                    "https://cdn.discordapp.com/attachments/707453916882665552/802673454163296256/12panicatthecrisco2.png"
            ))
            .put("Doofen No Shmirtzing", new TheSwiper(
                    "Doofen No Shmirtzing",
                    "Dr. Doofenshmirtz Spotted Nearby!",
                    "https://cdn.discordapp.com/attachments/707453916882665552/802675752503214090/Heinz_Doofenshmirtz.png",
                    "https://cdn.discordapp.com/attachments/707453916882665552/802676338216927232/Perry2Bvs2BDoof2B1.png",
                    "Doofenshmirtz Evil Incorporated",
                    "https://cdn.discordapp.com/attachments/707453916882665552/802676528759570452/a5663531c7b804c93fe2f4ca1c9c4a0b.gif"
            ))
            .build();

    public static final String SPIDERMAN_PNG = "https://cdn.discordapp.com/attachments/707453916882665552/769837667617079316/spiderman.png";

    public Swiper() {
        requiredRole = Constants.ADMIN_ROLE;
        name = "swiper";
        help = "Manually triggering swiper has been disabled";
    }

    @Override
    public void execute(CommandEvent event) {
        doSwiper();
    }

    @Scheduled(cron = "0 20 0,4,8,16,20 * * ?", zone = "GMT")
    public void swiper() {
        doSwiper();
    }

    private void doSwiper() {
        SwampyGamesConfig swampyGamesConfig = getSwampyGamesConfig();

        TextChannel swampysChannel = philJda.getTextChannelsByName(Constants.SWAMPYS_CHANNEL, false).get(0);

        if (swampyGamesConfig.getSwiperAwaiting() != null) {
            swampysChannel.sendMessage("There is currently a swiper running, you cannot trigger another").queue();
            return;
        }

        Member member = null;
        List<DiscordUser> allUsers = discordUserRepository.findAll();
        Collections.shuffle(allUsers);

        for (DiscordUser winningUser : allUsers) {
            try {
                Member memberById = philJda.getGuilds().get(0).getMemberById(winningUser.getId());
                if (memberById != null
                        && !isNotParticipating(memberById)
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
        swampyGamesConfig.setSwiperSavior(null);
        swampyGamesConfig.setSwiperExpiration(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(15));
        swampyGamesConfig = swampyGamesConfigRepository.save(swampyGamesConfig);

        String description = "They're trying to steal from <@!" + member.getId() + ">\nType '" + swampyGamesConfig.getNoSwipingPhrase()
                + "' in this channel within 15 minutes to stop them!";

        MessageEmbed message = Constants.simpleEmbed(theSwiper.getSpottedPhrase(), description, theSwiper.getSpottedImage());

        swampysChannel.sendMessage(message).queue();
    }

    public static class TheSwiper {
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
